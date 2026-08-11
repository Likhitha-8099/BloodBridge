package com.bloodbridge.service.impl;

import com.bloodbridge.dto.request.LocationUpdateDTO;
import com.bloodbridge.dto.response.DonorLiveLocationDTO;
import com.bloodbridge.dto.response.EtaResultDTO;
import com.bloodbridge.dto.response.TrackingAnalyticsDTO;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorLiveLocation;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.EmergencyResponseStatus;
import com.bloodbridge.enums.TrackingStatus;
import com.bloodbridge.exception.BloodRequestNotFoundException;
import com.bloodbridge.exception.DonorProfileNotFoundException;
import com.bloodbridge.exception.InvalidRequestStateException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonorLiveLocationRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.EmergencyResponseRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.EmergencyResponseService;
import com.bloodbridge.service.EtaEngineService;
import com.bloodbridge.service.LocationService;
import com.bloodbridge.service.LocationTrackingService;
import com.bloodbridge.service.RealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Production-grade implementation of LocationTrackingService.
 *
 * <p>Pipeline for each incoming GPS update:
 * <ol>
 *   <li>Authenticate donor &amp; validate active accepted EmergencyResponse</li>
 *   <li>Smart-filter: skip updates with bad accuracy / < 10 m movement / < 5 s interval (unless status change)</li>
 *   <li>Compute Haversine distance to hospital &amp; ETA via EtaEngineService</li>
 *   <li>Determine movement state (MOVING vs STOPPED)</li>
 *   <li>Persist DonorLiveLocation row</li>
 *   <li>Broadcast WebSocket payload to /topic/hospitals/{hospitalId}/live-location</li>
 *   <li>Auto-trigger reachHospital() transition when distance &lt; 100 m</li>
 *   <li>Return assembled DonorLiveLocationDTO</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationTrackingServiceImpl implements LocationTrackingService {

    // ── Smart-filter constants ────────────────────────────────────────────────
    /** Minimum horizontal displacement (metres) to persist a new location row. */
    private static final double MIN_DISTANCE_METERS = 10.0;

    /** Minimum interval between persisted updates (seconds). */
    private static final long MIN_INTERVAL_SECONDS = 5L;

    /** Maximum acceptable accuracy radius (metres); coarser fixes are discarded. */
    private static final double MAX_ACCURACY_METERS = 100.0;

    /** Donor is considered MOVING when speed exceeds this threshold (km/h). */
    private static final double MOVING_SPEED_THRESHOLD_KMH = 0.5;

    /** Auto-arrival distance threshold (metres). */
    private static final double AUTO_ARRIVE_THRESHOLD_METERS = 100.0;

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final UserRepository userRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final EmergencyResponseRepository emergencyResponseRepository;
    private final DonorLiveLocationRepository donorLiveLocationRepository;
    private final LocationService locationService;
    private final EtaEngineService etaEngineService;
    private final RealtimeService realtimeService;
    private final EmergencyResponseService emergencyResponseService;

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    @Override
    @Transactional
    public DonorLiveLocationDTO processLocationUpdate(String donorEmail, LocationUpdateDTO dto) {
        long start = System.currentTimeMillis();
        log.info("[GPS-STAGE-1] Processing location update from donor={} for requestId={}",
                donorEmail, dto.getBloodRequestId());

        // ── Stage 1: Resolve donor ────────────────────────────────────────────
        User user = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + donorEmail));
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found for: " + donorEmail));

        // ── Stage 2: Validate blood request & accepted response ───────────────
        BloodRequest request = bloodRequestRepository.findById(dto.getBloodRequestId())
                .orElseThrow(() -> new BloodRequestNotFoundException(
                        "Blood request not found: " + dto.getBloodRequestId()));

        boolean hasAccepted = emergencyResponseRepository.existsByBloodRequestIdAndDonorIdAndStatus(
                request.getId(), donor.getId(), EmergencyResponseStatus.ACCEPTED);
        boolean isTravelling = emergencyResponseRepository.existsByBloodRequestIdAndDonorIdAndStatus(
                request.getId(), donor.getId(), EmergencyResponseStatus.STARTED_TRAVEL);

        if (!hasAccepted && !isTravelling) {
            throw new InvalidRequestStateException(
                    "Donor " + donorEmail + " has not accepted emergency request " + dto.getBloodRequestId());
        }
        log.info("[GPS-STAGE-2] Donor {} authorization confirmed for requestId={}", donorEmail, dto.getBloodRequestId());

        // ── Stage 3: Smart update filter ──────────────────────────────────────
        Hospital hospital = request.getHospital();
        Optional<DonorLiveLocation> lastOpt = donorLiveLocationRepository
                .findLatestByDonorIdAndBloodRequestId(donor.getId(), request.getId());

        if (lastOpt.isPresent()) {
            DonorLiveLocation last = lastOpt.get();
            boolean tooSoon = last.getLastUpdated() != null &&
                    java.time.Duration.between(last.getLastUpdated(), LocalDateTime.now()).getSeconds() < MIN_INTERVAL_SECONDS;
            boolean badAccuracy = dto.getAccuracyMeters() != null && dto.getAccuracyMeters() > MAX_ACCURACY_METERS;
            double movementM = locationService.calculateDistance(
                    last.getLatitude(), last.getLongitude(),
                    dto.getLatitude(), dto.getLongitude()) * 1000.0;
            boolean tooClose = movementM < MIN_DISTANCE_METERS;

            if ((tooSoon || badAccuracy || tooClose) && dto.getSpeedKmh() == null) {
                log.debug("[GPS-STAGE-3-SKIP] Filtered update: tooSoon={} badAccuracy={} tooClose={}m",
                        tooSoon, badAccuracy, movementM);
                // Return last known state without persisting
                return buildDto(last, donor, hospital);
            }
        }
        log.info("[GPS-STAGE-3] Smart filter passed for donor={}", donorEmail);

        // ── Stage 4: Compute distance & ETA ───────────────────────────────────
        double distKm = (hospital.getLatitude() != null && hospital.getLongitude() != null)
                ? locationService.calculateDistance(
                        dto.getLatitude(), dto.getLongitude(),
                        hospital.getLatitude(), hospital.getLongitude())
                : 0.0;

        EtaResultDTO eta = etaEngineService.calculateEta(
                dto.getLatitude(), dto.getLongitude(),
                hospital.getLatitude(), hospital.getLongitude());

        log.info("[GPS-STAGE-4] Computed: distanceKm={} etaMinutes={}", distKm, eta.getEtaMinutes());

        // ── Stage 5: Determine tracking status ────────────────────────────────
        double distMeters = distKm * 1000.0;
        TrackingStatus status;
        if (distMeters <= AUTO_ARRIVE_THRESHOLD_METERS) {
            status = TrackingStatus.REACHED;
        } else if (dto.getSpeedKmh() != null && dto.getSpeedKmh() > MOVING_SPEED_THRESHOLD_KMH) {
            status = TrackingStatus.MOVING;
        } else {
            status = TrackingStatus.STOPPED;
        }
        log.info("[GPS-STAGE-5] Tracking status resolved to {} for donor={}", status, donorEmail);

        // ── Stage 6: Persist DonorLiveLocation ────────────────────────────────
        LocalDateTime fix = dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now();
        DonorLiveLocation location = DonorLiveLocation.builder()
                .donorId(donor.getId())
                .bloodRequestId(request.getId())
                .hospitalId(hospital.getId())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .accuracyMeters(dto.getAccuracyMeters())
                .speedKmh(dto.getSpeedKmh())
                .headingDegrees(dto.getHeadingDegrees())
                .altitudeMeters(dto.getAltitudeMeters())
                .batteryLevel(dto.getBatteryLevel())
                .distanceRemainingKm(distKm)
                .etaMinutes(eta.getEtaMinutes())
                .trackingStatus(status)
                .lastUpdated(fix)
                .build();

        DonorLiveLocation saved = donorLiveLocationRepository.save(location);
        log.info("[GPS-STAGE-6] Persisted DonorLiveLocation id={} for donor={}", saved.getId(), donorEmail);

        // ── Stage 7: Async WebSocket broadcast ────────────────────────────────
        DonorLiveLocationDTO responseDto = buildDto(saved, donor, hospital);
        broadcastLiveLocation(hospital.getId(), responseDto);

        // ── Stage 8: Auto-arrival transition ─────────────────────────────────
        if (status == TrackingStatus.REACHED) {
            log.info("[GPS-STAGE-8] Auto-arrival triggered: donor={} is within {}m of hospital={}",
                    donorEmail, distMeters, hospital.getHospitalName());
            try {
                emergencyResponseService.reachHospital(donorEmail, request.getId());
            } catch (Exception ex) {
                log.warn("[GPS-STAGE-8] reachHospital() already processed or error: {}", ex.getMessage());
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("[GPS-COMPLETE] Location update processed in {}ms for donor={}", elapsed, donorEmail);
        return responseDto;
    }

    @Override
    public List<DonorLiveLocationDTO> getLiveLocationsForRequest(Long requestId) {
        List<DonorLiveLocation> locations =
                donorLiveLocationRepository.findLatestForAllDonorsByBloodRequestId(requestId);

        return locations.stream()
                .map(loc -> {
                    DonorProfile donor = donorProfileRepository.findById(loc.getDonorId()).orElse(null);
                    Hospital hospital = bloodRequestRepository.findById(loc.getBloodRequestId())
                            .map(BloodRequest::getHospital).orElse(null);
                    return buildDto(loc, donor, hospital);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DonorLiveLocationDTO> getRouteHistory(Long requestId, Long donorId) {
        List<DonorLiveLocation> route =
                donorLiveLocationRepository.findRouteByDonorIdAndBloodRequestId(donorId, requestId);

        return route.stream()
                .map(loc -> {
                    DonorProfile donor = donorProfileRepository.findById(loc.getDonorId()).orElse(null);
                    Hospital hospital = bloodRequestRepository.findById(loc.getBloodRequestId())
                            .map(BloodRequest::getHospital).orElse(null);
                    return buildDto(loc, donor, hospital);
                })
                .collect(Collectors.toList());
    }

    @Override
    public TrackingAnalyticsDTO getTrackingAnalytics() {
        List<TrackingStatus> activeStatuses = Arrays.asList(
                TrackingStatus.STARTED, TrackingStatus.MOVING, TrackingStatus.STOPPED);

        long activeSessions = donorLiveLocationRepository.findByTrackingStatusIn(activeStatuses).stream()
                .map(DonorLiveLocation::getDonorId)
                .distinct()
                .count();

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        Double avgEta = donorLiveLocationRepository.findAverageEtaMinutesSince(since);
        Double avgSpeed = donorLiveLocationRepository.findAverageSpeedKmhSince(since);

        long total = donorLiveLocationRepository.count();
        long reached = donorLiveLocationRepository.findByTrackingStatusIn(
                Arrays.asList(TrackingStatus.REACHED, TrackingStatus.COMPLETED)).stream()
                .map(DonorLiveLocation::getDonorId)
                .distinct()
                .count();

        double completionRate = total > 0 ? (reached * 100.0 / total) : 0.0;

        return TrackingAnalyticsDTO.builder()
                .activeTrackingSessions(activeSessions)
                .averageEtaMinutes(avgEta)
                .averageSpeedKmh(avgSpeed)
                .completionRatePercent(completionRate)
                .totalTrackingRecords(total)
                .build();
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    @Async
    protected void broadcastLiveLocation(Long hospitalId, DonorLiveLocationDTO payload) {
        try {
            String topic = "/topic/hospitals/" + hospitalId + "/live-location";
            realtimeService.publishGlobalEvent(topic, payload);
            log.debug("[GPS-WS] Broadcasted live location for hospitalId={} on topic={}", hospitalId, topic);
        } catch (Exception ex) {
            log.error("[GPS-WS] WebSocket broadcast failed for hospitalId={}: {}", hospitalId, ex.getMessage(), ex);
        }
    }

    private DonorLiveLocationDTO buildDto(DonorLiveLocation loc, DonorProfile donor, Hospital hospital) {
        String donorName = "";
        String donorEmail = "";
        String donorPhone = "";
        String donorBloodGroup = "";

        if (donor != null) {
            donorEmail = donor.getEmail();
            donorBloodGroup = donor.getBloodGroup() != null ? donor.getBloodGroup().name() : "";
            User u = donor.getUser();
            if (u != null) {
                donorName = u.getFullName() != null ? u.getFullName() : "";
                donorPhone = u.getPhoneNumber();
            }
        }

        String hospitalName = "";
        Double hospitalLat = null;
        Double hospitalLon = null;
        if (hospital != null) {
            hospitalName = hospital.getHospitalName();
            hospitalLat = hospital.getLatitude();
            hospitalLon = hospital.getLongitude();
        }

        double distKm = loc.getDistanceRemainingKm() != null ? loc.getDistanceRemainingKm() : 0.0;
        String distFormatted = distKm < 1.0
                ? String.format("%.0f m", distKm * 1000)
                : String.format("%.1f km", distKm);

        int etaMins = loc.getEtaMinutes() != null ? loc.getEtaMinutes() : 0;
        String etaFormatted = etaMins == 0 ? "Arrived" : etaMins + " min" + (etaMins != 1 ? "s" : "");

        // Google Maps navigation deep-link (donor → hospital)
        String navUrl = (hospitalLat != null && hospitalLon != null)
                ? String.format("https://www.google.com/maps/dir/?api=1&origin=%f,%f&destination=%f,%f&travelmode=driving",
                        loc.getLatitude(), loc.getLongitude(), hospitalLat, hospitalLon)
                : null;

        // Google Maps embed showing both donor position and hospital
        String embedUrl = (hospitalLat != null && hospitalLon != null)
                ? String.format("https://www.google.com/maps/embed/v1/directions?key=&origin=%f,%f&destination=%f,%f&mode=driving",
                        loc.getLatitude(), loc.getLongitude(), hospitalLat, hospitalLon)
                : null;

        return DonorLiveLocationDTO.builder()
                .trackingId(loc.getId())
                .donorId(loc.getDonorId())
                .bloodRequestId(loc.getBloodRequestId())
                .hospitalId(loc.getHospitalId())
                .donorName(donorName)
                .donorEmail(donorEmail)
                .donorPhone(donorPhone)
                .donorBloodGroup(donorBloodGroup)
                .hospitalName(hospitalName)
                .hospitalLatitude(hospitalLat)
                .hospitalLongitude(hospitalLon)
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .accuracyMeters(loc.getAccuracyMeters())
                .speedKmh(loc.getSpeedKmh())
                .headingDegrees(loc.getHeadingDegrees())
                .altitudeMeters(loc.getAltitudeMeters())
                .batteryLevel(loc.getBatteryLevel())
                .distanceRemainingKm(distKm)
                .distanceRemainingFormatted(distFormatted)
                .etaMinutes(etaMins)
                .etaFormatted(etaFormatted)
                .googleMapsNavigationUrl(navUrl)
                .googleMapsEmbedUrl(embedUrl)
                .trackingStatus(loc.getTrackingStatus())
                .lastUpdated(loc.getLastUpdated())
                .createdAt(loc.getCreatedAt())
                .build();
    }

}
