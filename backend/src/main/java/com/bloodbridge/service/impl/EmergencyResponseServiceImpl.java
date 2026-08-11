package com.bloodbridge.service.impl;

import com.bloodbridge.dto.request.AcceptEmergencyRequestDTO;
import com.bloodbridge.dto.request.RejectEmergencyRequestDTO;
import com.bloodbridge.dto.response.EmergencyResponseDTO;
import com.bloodbridge.dto.response.HospitalEmergencyLiveStatsDTO;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.EmergencyResponse;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.dto.RealtimeEventDTO;
import com.bloodbridge.enums.EmailDeliveryStatus;
import com.bloodbridge.enums.EmergencyResponseStatus;
import com.bloodbridge.enums.RealtimeEventType;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.event.DonorAcceptedEvent;
import com.bloodbridge.event.DonorRejectedEvent;
import com.bloodbridge.exception.BloodRequestNotFoundException;
import com.bloodbridge.exception.DonorProfileNotFoundException;
import com.bloodbridge.exception.InvalidRequestStateException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.EmailNotificationRepository;
import com.bloodbridge.repository.EmergencyResponseRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.EmergencyResponseService;
import com.bloodbridge.service.LocationService;
import com.bloodbridge.service.RealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Production-grade implementation of EmergencyResponseService.
 * Controls donor response flow, auto-close threshold logic, Google Maps navigation links,
 * real-time WebSocket events, and structured Stage 1-8 telemetry logging.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmergencyResponseServiceImpl implements EmergencyResponseService {

    private final EmergencyResponseRepository emergencyResponseRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final UserRepository userRepository;
    private final EmailNotificationRepository emailNotificationRepository;
    private final LocationService locationService;
    private final RealtimeService realtimeService;
    private final ApplicationEventPublisher eventPublisher;
    private final com.bloodbridge.service.EmergencyTimelineService timelineService;
    private final com.bloodbridge.service.SmartDonorMatchingPipelineService pipelineService;

    @Override
    @Transactional
    public EmergencyResponseDTO acceptEmergencyRequest(String donorEmail, AcceptEmergencyRequestDTO dto) {
        long startTime = System.currentTimeMillis();

        User user = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found for email: " + donorEmail));

        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found for user: " + donorEmail));

        BloodRequest request = bloodRequestRepository.findById(dto.getEmergencyRequestId())
                .orElseThrow(() -> new BloodRequestNotFoundException("Emergency Blood Request not found for ID: " + dto.getEmergencyRequestId()));

        // Security & Status Checks
        if (request.getStatus() == RequestStatus.CANCELLED) {
            throw new InvalidRequestStateException("Cannot accept a CANCELLED emergency blood request");
        }
        if (request.getStatus() == RequestStatus.COMPLETED || request.getStatus() == RequestStatus.FULFILLED) {
            throw new InvalidRequestStateException("Cannot accept an emergency blood request that is already COMPLETED");
        }

        // Duplicate ACCEPT check
        if (emergencyResponseRepository.existsByBloodRequestIdAndDonorIdAndStatus(request.getId(), donor.getId(), EmergencyResponseStatus.ACCEPTED)) {
            throw new IllegalArgumentException("Donor has already accepted this emergency blood request");
        }

        Hospital hospital = request.getHospital();
        Double hospLat = hospital != null ? hospital.getLatitude() : null;
        Double hospLon = hospital != null ? hospital.getLongitude() : null;
        Double donorLat = donor.getLatitude();
        Double donorLon = donor.getLongitude();

        double distanceKm = (hospLat != null && hospLon != null && donorLat != null && donorLon != null)
                ? locationService.calculateDistance(hospLat, hospLon, donorLat, donorLon)
                : 5.0;

        LocalDateTime now = LocalDateTime.now();
        long responseTimeSeconds = request.getCreatedAt() != null ? Duration.between(request.getCreatedAt(), now).getSeconds() : 0;

        EmergencyResponse response = emergencyResponseRepository.findByBloodRequestIdAndDonorId(request.getId(), donor.getId())
                .orElse(EmergencyResponse.builder()
                        .bloodRequest(request)
                        .donor(donor)
                        .createdAt(now)
                        .build());

        response.setStatus(EmergencyResponseStatus.ACCEPTED);
        response.setAcceptedAt(now);
        response.setResponseTimeSeconds(responseTimeSeconds);
        response.setDistanceKm(distanceKm);
        response.setEtaMinutes(dto.getEtaMinutes() != null ? dto.getEtaMinutes() : 15);
        response.setRemarks(dto.getRemarks());

        EmergencyResponse savedResponse = emergencyResponseRepository.save(response);

        log.info("================================================================================");
        log.info("[STAGE 6: DONOR ACCEPTED]");
        log.info(" - Emergency Request ID : #{}", request.getId());
        log.info(" - Donor Name / Email   : {} ({})", user.getFullName(), donorEmail);
        log.info(" - Hospital             : {}", hospital != null ? hospital.getHospitalName() : "Hospital");
        log.info(" - Distance             : {} KM", distanceKm);
        log.info(" - ETA                  : {} Minutes", dto.getEtaMinutes());
        log.info(" - Response Time        : {} Seconds", responseTimeSeconds);
        log.info(" - Thread               : {}", Thread.currentThread().getName());

        // Publish Spring Event for Real-Time STOMP dispatches
        eventPublisher.publishEvent(new DonorAcceptedEvent(this, savedResponse));

        // Module 3: Broadcast Real-Time Update to Hospital STOMP Subscribers
        if (hospital != null && hospital.getUser() != null) {
            HospitalEmergencyLiveStatsDTO liveStats = getHospitalLiveStats(request.getId());
            RealtimeEventDTO hospitalEvent = RealtimeEventDTO.of(
                    RealtimeEventType.EMERGENCY_REQUEST_ALERT,
                    "EMERGENCY_RESPONSE",
                    request.getId(),
                    "Donor Accepted",
                    "Donor Accepted Emergency Request",
                    liveStats
            );
            realtimeService.publishHospitalUpdate(hospital.getId(), hospitalEvent);
            log.info("[STAGE 7: HOSPITAL DASHBOARD UPDATED]");
            log.info(" - Hospital ID          : {}", hospital.getId());
            log.info(" - Live Accepted Donors : {}", liveStats.getAcceptedCount());
        }

        // Module 5: Auto-Close Threshold Check
        long acceptedCount = emergencyResponseRepository.countByBloodRequestIdAndStatus(request.getId(), EmergencyResponseStatus.ACCEPTED);
        if (acceptedCount >= request.getUnitsRequired()) {
            log.info("================================================================================");
            log.info("[STAGE 8: EMERGENCY MATCHED - IN PROGRESS]");
            log.info(" - Emergency Request ID : #{}", request.getId());
            log.info(" - Required Units       : {}", request.getUnitsRequired());
            log.info(" - Total Accepted       : {}", acceptedCount);
            log.info(" - Action               : Threshold Met. Transitioning Status to IN_PROGRESS.");

            request.setStatus(RequestStatus.IN_PROGRESS);
            bloodRequestRepository.save(request);

            // Broadcast real-time STOMP alert to close donor popups across all connected clients
            realtimeService.publishGlobalEvent("/topic/donors/emergency-closed/" + request.getId(), "EMERGENCY_CLOSED");
            log.info("================================================================================");
        }

        long executionTimeMs = System.currentTimeMillis() - startTime;
        log.info("[DONOR-ACCEPT-COMPLETED] Processed in {} ms", executionTimeMs);
        log.info("================================================================================");

        return mapToDTO(savedResponse);
    }

    @Override
    @Transactional
    public EmergencyResponseDTO rejectEmergencyRequest(String donorEmail, RejectEmergencyRequestDTO dto) {
        User user = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found for email: " + donorEmail));

        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found for user: " + donorEmail));

        BloodRequest request = bloodRequestRepository.findById(dto.getEmergencyRequestId())
                .orElseThrow(() -> new BloodRequestNotFoundException("Emergency Blood Request not found for ID: " + dto.getEmergencyRequestId()));

        LocalDateTime now = LocalDateTime.now();
        EmergencyResponse response = emergencyResponseRepository.findByBloodRequestIdAndDonorId(request.getId(), donor.getId())
                .orElse(EmergencyResponse.builder()
                        .bloodRequest(request)
                        .donor(donor)
                        .createdAt(now)
                        .build());

        response.setStatus(EmergencyResponseStatus.REJECTED);
        response.setRejectedAt(now);
        response.setRemarks(dto.getReason());

        EmergencyResponse saved = emergencyResponseRepository.save(response);
        eventPublisher.publishEvent(new DonorRejectedEvent(this, saved));

        log.info("[DONOR-REJECTED] Donor ID {} ({}) rejected Request #{}", donor.getId(), donorEmail, request.getId());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyResponseDTO> getMyResponses(String donorEmail) {
        User user = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + donorEmail));

        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found for user: " + donorEmail));

        List<EmergencyResponse> responses = emergencyResponseRepository.findByDonorIdWithDetails(donor.getId());
        return responses.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmergencyResponseDTO startTravel(String donorEmail, Long emergencyRequestId) {
        User user = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + donorEmail));
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found: " + donorEmail));
        EmergencyResponse response = emergencyResponseRepository.findByBloodRequestIdAndDonorId(emergencyRequestId, donor.getId())
                .orElseThrow(() -> new BloodRequestNotFoundException("Emergency response record not found"));

        response.setStatus(EmergencyResponseStatus.STARTED_TRAVEL);
        response.setStartedTravelAt(LocalDateTime.now());
        EmergencyResponse saved = emergencyResponseRepository.save(response);

        timelineService.recordEvent(emergencyRequestId, "STARTED_TRAVEL", "Donor Started Travel",
                user.getFullName() + " has started travelling to the hospital.", donorEmail, null);

        Hospital hospital = saved.getBloodRequest().getHospital();
        if (hospital != null) {
            realtimeService.publishHospitalUpdate(hospital.getId(), RealtimeEventDTO.of(
                    RealtimeEventType.EMERGENCY_REQUEST_ALERT, "DONOR_JOURNEY", emergencyRequestId,
                    "Donor Started Travel", user.getFullName() + " is en route", getHospitalLiveStats(emergencyRequestId)
            ));
        }

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public EmergencyResponseDTO reachHospital(String donorEmail, Long emergencyRequestId) {
        User user = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + donorEmail));
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found: " + donorEmail));
        EmergencyResponse response = emergencyResponseRepository.findByBloodRequestIdAndDonorId(emergencyRequestId, donor.getId())
                .orElseThrow(() -> new BloodRequestNotFoundException("Emergency response record not found"));

        response.setStatus(EmergencyResponseStatus.REACHED_HOSPITAL);
        response.setReachedHospitalAt(LocalDateTime.now());
        EmergencyResponse saved = emergencyResponseRepository.save(response);

        timelineService.recordEvent(emergencyRequestId, "REACHED_HOSPITAL", "Donor Reached Hospital",
                user.getFullName() + " has arrived at the hospital facility.", donorEmail, null);

        Hospital hospital = saved.getBloodRequest().getHospital();
        if (hospital != null) {
            realtimeService.publishHospitalUpdate(hospital.getId(), RealtimeEventDTO.of(
                    RealtimeEventType.EMERGENCY_REQUEST_ALERT, "DONOR_JOURNEY", emergencyRequestId,
                    "Donor Reached Hospital", user.getFullName() + " arrived at hospital", getHospitalLiveStats(emergencyRequestId)
            ));
        }

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public EmergencyResponseDTO completeDonation(String donorEmail, Long emergencyRequestId) {
        User user = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + donorEmail));
        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found: " + donorEmail));
        EmergencyResponse response = emergencyResponseRepository.findByBloodRequestIdAndDonorId(emergencyRequestId, donor.getId())
                .orElseThrow(() -> new BloodRequestNotFoundException("Emergency response record not found"));

        LocalDateTime now = LocalDateTime.now();
        response.setStatus(EmergencyResponseStatus.DONATION_COMPLETED);
        response.setCompletedDonationAt(now);
        response.setRewardGeneratedAt(now);
        EmergencyResponse saved = emergencyResponseRepository.save(response);

        timelineService.recordEvent(emergencyRequestId, "DONATION_COMPLETED", "Blood Donation Completed",
                user.getFullName() + " completed blood donation. Reward badge generated!", donorEmail, null);

        Hospital hospital = saved.getBloodRequest().getHospital();
        if (hospital != null) {
            realtimeService.publishHospitalUpdate(hospital.getId(), RealtimeEventDTO.of(
                    RealtimeEventType.EMERGENCY_REQUEST_ALERT, "DONOR_JOURNEY", emergencyRequestId,
                    "Donation Completed", user.getFullName() + " completed donation!", getHospitalLiveStats(emergencyRequestId)
            ));
        }

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void cancelEmergencyByHospital(Long emergencyRequestId, String hospitalEmail) {
        BloodRequest request = bloodRequestRepository.findById(emergencyRequestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Emergency request not found: " + emergencyRequestId));

        request.setStatus(RequestStatus.CANCELLED);
        bloodRequestRepository.save(request);

        // Cancel pending email notifications
        var pendingEmails = emailNotificationRepository.findAll().stream()
                .filter(e -> Objects.equals(e.getEmergencyRequestId(), emergencyRequestId) && e.getStatus() == EmailDeliveryStatus.PENDING)
                .toList();
        for (var emailNotif : pendingEmails) {
            emailNotif.setStatus(EmailDeliveryStatus.FAILED);
            emailNotif.setFailureReason("Emergency cancelled by hospital");
            emailNotificationRepository.save(emailNotif);
        }

        // Broadcast popup dismissal
        realtimeService.publishGlobalEvent("/topic/donors/emergency-closed/" + emergencyRequestId, "EMERGENCY_CANCELLED");

        timelineService.recordEvent(emergencyRequestId, "EMERGENCY_CANCELLED", "Emergency Request Cancelled",
                "Hospital cancelled emergency request #" + emergencyRequestId, hospitalEmail, null);

        log.info("[EMERGENCY-CANCELLED] Emergency Request #{} cancelled by {}", emergencyRequestId, hospitalEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public HospitalEmergencyLiveStatsDTO getHospitalLiveStats(Long requestId) {
        BloodRequest request = bloodRequestRepository.findById(requestId).orElse(null);
        if (request == null) {
            return HospitalEmergencyLiveStatsDTO.builder().emergencyRequestId(requestId).build();
        }

        List<EmergencyResponse> allResponses = emergencyResponseRepository.findAll().stream()
                .filter(r -> r.getBloodRequest() != null && Objects.equals(r.getBloodRequest().getId(), requestId))
                .toList();

        List<EmergencyResponse> acceptedResponses = allResponses.stream()
                .filter(r -> r.getStatus() == EmergencyResponseStatus.ACCEPTED || r.getStatus() == EmergencyResponseStatus.STARTED_TRAVEL || r.getStatus() == EmergencyResponseStatus.REACHED_HOSPITAL || r.getStatus() == EmergencyResponseStatus.DONATION_COMPLETED)
                .toList();

        long acceptedCount = acceptedResponses.size();
        long rejectedCount = allResponses.stream().filter(r -> r.getStatus() == EmergencyResponseStatus.REJECTED).count();
        long pendingCount = allResponses.stream().filter(r -> r.getStatus() == EmergencyResponseStatus.PENDING).count();
        long travellingCount = allResponses.stream().filter(r -> r.getStatus() == EmergencyResponseStatus.STARTED_TRAVEL).count();
        long reachedHospitalCount = allResponses.stream().filter(r -> r.getStatus() == EmergencyResponseStatus.REACHED_HOSPITAL).count();
        int unitsCollected = (int) allResponses.stream().filter(r -> r.getStatus() == EmergencyResponseStatus.DONATION_COMPLETED).count();

        long emailsSent = emailNotificationRepository.countByEmergencyRequestIdAndStatus(requestId, EmailDeliveryStatus.SENT);
        long emailsFailed = emailNotificationRepository.countByEmergencyRequestIdAndStatus(requestId, EmailDeliveryStatus.FAILED);

        Double avgResponseTime = emergencyResponseRepository.findAverageResponseTimeSecondsByBloodRequestId(requestId);

        int unitsReq = request.getUnitsRequired() != null ? request.getUnitsRequired() : 1;
        int remaining = Math.max(0, unitsReq - unitsCollected);

        long totalResponded = acceptedCount + rejectedCount;
        long totalDonorsScanned = totalResponded + pendingCount;
        double acceptanceRate = totalResponded > 0 ? (acceptedCount * 100.0) / totalResponded : 0.0;
        double responseRate = totalDonorsScanned > 0 ? (totalResponded * 100.0) / totalDonorsScanned : 0.0;

        double avgEta = acceptedResponses.stream()
                .map(EmergencyResponse::getEtaMinutes)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(15.0);

        List<EmergencyResponseDTO> acceptedDTOs = acceptedResponses.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        // Phase 3D.1: Compute 4 Section Matching Telemetry (Stage 7)
        com.bloodbridge.dto.response.SmartDonorMatchingPipelineDTO pipelineDTO = null;
        try {
            if (request.getHospital() != null && request.getHospital().isApprovedOrVerified()) {
                pipelineDTO = pipelineService.executePipeline(request);
            }
        } catch (Exception e) {
            log.warn("[LIVE-STATS-PIPELINE-WARN] Could not compute matching pipeline for request #{}: {}", requestId, e.getMessage());
        }

        return HospitalEmergencyLiveStatsDTO.builder()
                .emergencyRequestId(requestId)
                .hospitalName(request.getHospital() != null ? request.getHospital().getHospitalName() : "Hospital")
                .bloodGroupNeeded(request.getBloodGroupNeeded() != null ? request.getBloodGroupNeeded().name() : "")
                .unitsRequired(unitsReq)
                .remainingUnitsNeeded(remaining)
                .requestStatus(request.getStatus() != null ? request.getStatus().name() : "PENDING")
                .totalEligibleDonors(totalDonorsScanned)
                .matchedDonors(totalDonorsScanned)
                .emailsSent(emailsSent)
                .emailsDelivered(emailsSent)
                .emailsFailed(emailsFailed)
                .pendingCount(pendingCount)
                .acceptedCount(acceptedCount)
                .rejectedCount(rejectedCount)
                .travellingCount(travellingCount)
                .reachedHospitalCount(reachedHospitalCount)
                .unitsCollected(unitsCollected)
                .acceptanceRate(Math.round(acceptanceRate * 100.0) / 100.0)
                .responseRate(Math.round(responseRate * 100.0) / 100.0)
                .averageEta(Math.round(avgEta * 100.0) / 100.0)
                .averageResponseTimeSeconds(avgResponseTime != null ? Math.round(avgResponseTime * 100.0) / 100.0 : 0.0)
                .immediateMatches(pipelineDTO != null ? pipelineDTO.getGroupA() : null)
                .nearbyCompatible(pipelineDTO != null ? pipelineDTO.getGroupB() : null)
                .extendedCompatible(pipelineDTO != null ? pipelineDTO.getGroupC() : null)
                .emergencyBroadcast(pipelineDTO != null ? pipelineDTO.getGroupD() : null)
                .acceptedDonors(acceptedDTOs)
                .build();
    }

    private EmergencyResponseDTO mapToDTO(EmergencyResponse r) {
        DonorProfile d = r.getDonor();
        User u = d != null ? d.getUser() : null;
        BloodRequest req = r.getBloodRequest();
        Hospital h = req != null ? req.getHospital() : null;

        Double hospLat = h != null ? h.getLatitude() : null;
        Double hospLon = h != null ? h.getLongitude() : null;

        // Module 4: Google Maps Navigation URL Format
        String mapsUrl = (hospLat != null && hospLon != null)
                ? String.format("https://www.google.com/maps/dir/?api=1&destination=%f,%f", hospLat, hospLon)
                : "https://www.google.com/maps";

        return EmergencyResponseDTO.builder()
                .id(r.getId())
                .emergencyRequestId(req != null ? req.getId() : null)
                .donorId(d != null ? d.getId() : null)
                .donorName(u != null ? u.getFullName() : "Anonymous Donor")
                .donorPhone(u != null ? u.getPhoneNumber() : "")
                .donorEmail(u != null ? u.getEmail() : (d != null ? d.getEmail() : ""))
                .donorBloodGroup(d != null && d.getBloodGroup() != null ? d.getBloodGroup().name() : "")
                .status(r.getStatus())
                .distanceKm(r.getDistanceKm())
                .etaMinutes(r.getEtaMinutes())
                .responseTimeSeconds(r.getResponseTimeSeconds())
                .googleMapsUrl(mapsUrl)
                .acceptedAt(r.getAcceptedAt())
                .rejectedAt(r.getRejectedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
