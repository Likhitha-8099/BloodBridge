package com.bloodbridge.service.impl;

import com.bloodbridge.config.MatchingConfig;
import com.bloodbridge.dto.RealtimeEventDTO;
import com.bloodbridge.dto.response.*;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.MatchedEmergencyDonor;
import com.bloodbridge.entity.MatchingAnalytics;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.EmergencyResponseStatus;
import com.bloodbridge.enums.MatchedEmergencyDonorStatus;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.enums.RealtimeEventType;
import com.bloodbridge.exception.BloodRequestNotFoundException;
import com.bloodbridge.notification.NotificationOrchestrator;
import com.bloodbridge.notification.NotificationPayload;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.EmergencyResponseRepository;
import com.bloodbridge.repository.MatchedEmergencyDonorRepository;
import com.bloodbridge.repository.MatchingAnalyticsRepository;
import com.bloodbridge.service.LocationService;
import com.bloodbridge.service.NotificationService;
import com.bloodbridge.service.RealtimeService;
import com.bloodbridge.service.SmartDonorMatchingPipelineService;
import com.bloodbridge.util.BloodCompatibilityMatrix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enterprise implementation of the 10-Stage Smart Donor Matching Pipeline (Blood Compatibility First).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmartDonorMatchingPipelineServiceImpl implements SmartDonorMatchingPipelineService {

    private final MatchingConfig matchingConfig;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final EmergencyResponseRepository emergencyResponseRepository;
    private final MatchingAnalyticsRepository matchingAnalyticsRepository;
    private final MatchedEmergencyDonorRepository matchedEmergencyDonorRepository;
    private final LocationService locationService;
    private final NotificationService notificationService;
    private final RealtimeService realtimeService;
    private final NotificationOrchestrator notificationOrchestrator;

    @Override
    @Transactional
    public SmartDonorMatchingPipelineDTO executePipeline(BloodRequest request) {
        long startTimeMs = System.currentTimeMillis();

        // ─────────────────────────────────────────────────────────────────────────────
        // STAGE 1: Validate Blood Request
        // ─────────────────────────────────────────────────────────────────────────────
        validateBloodRequest(request);

        Hospital hospital = request.getHospital();
        BloodGroup neededGroup = request.getBloodGroupNeeded();

        log.info("================================================================================");
        log.info("[SMART-PIPELINE-START] Executing 10-Stage Matching Pipeline for Blood Request #{}", request.getId());
        log.info(" - Hospital       : {} (ID={})", hospital.getHospitalName(), hospital.getId());
        log.info(" - Needed Group   : {}", neededGroup);
        log.info(" - Units Required : {}", request.getUnitsRequired());
        log.info(" - Config Radii   : Primary={} KM, Secondary={} KM, Tertiary={} KM",
                matchingConfig.getRadius().getPrimary(),
                matchingConfig.getRadius().getSecondary(),
                matchingConfig.getRadius().getTertiary());

        List<DonorProfile> allDonors = donorProfileRepository.findAll();
        long totalEvaluated = allDonors.size();

        // ─────────────────────────────────────────────────────────────────────────────
        // STAGE 2: Medical Blood Compatibility Filtering
        // ─────────────────────────────────────────────────────────────────────────────
        List<BloodGroup> compatibleGroups = BloodCompatibilityMatrix.getCompatibleDonorGroups(neededGroup);
        log.info("[STAGE-2-MEDICAL] Compatible donor groups for {}: {}", neededGroup, compatibleGroups);

        List<DonorProfile> medicallyCompatibleDonors = allDonors.stream()
                .filter(d -> d.getBloodGroup() != null && BloodCompatibilityMatrix.isCompatible(d.getBloodGroup(), neededGroup))
                .collect(Collectors.toList());

        long compatibleCount = medicallyCompatibleDonors.size();
        log.info("[STAGE-2-MEDICAL] Found {} medically compatible donors out of {}", compatibleCount, totalEvaluated);

        // ─────────────────────────────────────────────────────────────────────────────
        // STAGE 3: Eligibility Filtering
        // ─────────────────────────────────────────────────────────────────────────────
        List<DonorProfile> eligibleDonors = new ArrayList<>();
        Map<Long, Double> donorDistances = new HashMap<>();

        double hospLat = (hospital.getLatitude() != null && hospital.getLatitude() != 0.0) ? hospital.getLatitude() : 12.9720;
        double hospLon = (hospital.getLongitude() != null && hospital.getLongitude() != 0.0) ? hospital.getLongitude() : 77.5950;

        for (DonorProfile donor : medicallyCompatibleDonors) {
            User user = donor.getUser();

            // Rule 3.1: Active Account & Non-deleted/non-blocked
            if (user == null || !Boolean.TRUE.equals(user.getActive()) ||
                    "INACTIVE".equalsIgnoreCase(donor.getStatus()) ||
                    "DELETED".equalsIgnoreCase(donor.getStatus()) ||
                    "BLOCKED".equalsIgnoreCase(donor.getStatus())) {
                log.info("[STAGE-3-SKIP] Donor ID #{}: Inactive account, deleted, or blocked (Status: {})", donor.getId(), donor.getStatus());
                continue;
            }

            // Rule 3.2: Email Verified (Log INFO warning if unverified, but allow active donors to receive emergency alerts)
            if (user.getEmailVerified() != null && !Boolean.TRUE.equals(user.getEmailVerified())) {
                log.info("[STAGE-3-INFO] Donor ID #{}: Account email unverified, but proceeding for emergency matching", donor.getId());
            }

            // Rule 3.3: Availability Check
            if (!Boolean.TRUE.equals(donor.getIsAvailableForDonation()) || !Boolean.TRUE.equals(donor.getEmergencyAvailable())) {
                log.info("[STAGE-3-SKIP] Donor ID #{}: Not available for donation (Available: {}, EmergencyAvailable: {})",
                        donor.getId(), donor.getIsAvailableForDonation(), donor.getEmergencyAvailable());
                continue;
            }

            // Rule 3.4: Cooldown Period Check
            int cooldownDays = matchingConfig.getCooldownDays();
            if (donor.getLastDonationDate() != null && donor.getLastDonationDate().isAfter(LocalDate.now().minusDays(cooldownDays))) {
                log.info("[STAGE-3-SKIP] Donor ID #{}: Donated within cooldown period ({})", donor.getId(), donor.getLastDonationDate());
                continue;
            }
            if (donor.getNextEligibleDate() != null && donor.getNextEligibleDate().isAfter(LocalDate.now())) {
                log.info("[STAGE-3-SKIP] Donor ID #{}: Next eligible date in future ({})", donor.getId(), donor.getNextEligibleDate());
                continue;
            }

            // Rule 3.5: GPS Location Requirement (Check DonorProfile first, fallback to User entity, then regional fallback 12.9716, 77.5946)
            Double dLat = donor.getLatitude() != null ? donor.getLatitude() : (user != null && user.getLatitude() != null ? user.getLatitude() : 12.9716);
            Double dLon = donor.getLongitude() != null ? donor.getLongitude() : (user != null && user.getLongitude() != null ? user.getLongitude() : 77.5946);

            // Rule 3.6: Accepted Another Active Emergency Check
            boolean alreadyAccepted = emergencyResponseRepository.findByDonorIdWithDetails(donor.getId()).stream()
                    .anyMatch(r -> r.getStatus() == EmergencyResponseStatus.ACCEPTED &&
                            r.getBloodRequest() != null &&
                            !"FULFILLED".equalsIgnoreCase(r.getBloodRequest().getStatus().name()) &&
                            !"CANCELLED".equalsIgnoreCase(r.getBloodRequest().getStatus().name()));
            if (alreadyAccepted) {
                log.info("[STAGE-3-SKIP] Donor ID #{}: Already accepted another active emergency", donor.getId());
                continue;
            }

            double distKm = locationService.calculateDistance(hospLat, hospLon, dLat, dLon);
            donorDistances.put(donor.getId(), distKm);
            eligibleDonors.add(donor);
            log.info("[STAGE-3-ELIGIBLE] Donor ID #{} ({}) passed eligibility criteria! Distance: {} KM", donor.getId(), donor.getEmail(), distKm);
        }

        long eligibleCount = eligibleDonors.size();
        long filteredCount = totalEvaluated - eligibleCount;
        log.info("[STAGE-3-ELIGIBILITY-COMPLETE] Passed {} eligible donors out of {} evaluated (Filtered out {})", eligibleCount, totalEvaluated, filteredCount);

        // ─────────────────────────────────────────────────────────────────────────────
        // STAGE 4: Priority Distance Grouping (Group A, B, C, D)
        // ─────────────────────────────────────────────────────────────────────────────
        double r1 = matchingConfig.getRadius().getPrimary();    // 50 KM
        double r2 = matchingConfig.getRadius().getSecondary();  // 75 KM
        double r3 = matchingConfig.getRadius().getTertiary();   // 100 KM

        List<DonorMatchSummaryDTO> groupADonors = new ArrayList<>();
        List<DonorMatchSummaryDTO> groupBDonors = new ArrayList<>();
        List<DonorMatchSummaryDTO> groupCDonors = new ArrayList<>();
        List<DonorMatchSummaryDTO> groupDDonors = new ArrayList<>();

        for (DonorProfile d : eligibleDonors) {
            double dist = donorDistances.getOrDefault(d.getId(), 0.0);
            DonorMatchSummaryDTO summary = toSummaryDTO(d, dist);

            if (dist <= r1) {
                groupADonors.add(summary);
            } else if (dist <= r2) {
                groupBDonors.add(summary);
            } else if (dist <= r3) {
                groupCDonors.add(summary);
            } else {
                groupDDonors.add(summary);
            }
        }

        // Sort Group A by: 1. distance (asc), 2. lastDonationDate (nulls/oldest first), 3. donorScore (desc)
        groupADonors.sort(Comparator.comparingDouble(DonorMatchSummaryDTO::getDistanceKm)
                .thenComparing(DonorMatchSummaryDTO::getLastDonationDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(Comparator.comparingDouble(DonorMatchSummaryDTO::getDonorScore).reversed()));

        groupBDonors.sort(Comparator.comparingDouble(DonorMatchSummaryDTO::getDistanceKm));
        groupCDonors.sort(Comparator.comparingDouble(DonorMatchSummaryDTO::getDistanceKm));
        groupDDonors.sort(Comparator.comparingDouble(DonorMatchSummaryDTO::getDistanceKm));

        log.info("[STAGE-4-DISTANCE-GROUPING] Evaluated Priority Groups -> Group A (0-{}KM): {}, Group B ({}-{}KM): {}, Group C ({}-{}KM): {}, Group D (>{}+KM): {}",
                r1, groupADonors.size(), r1, r2, groupBDonors.size(), r2, r3, groupCDonors.size(), r3, groupDDonors.size());

        // ─────────────────────────────────────────────────────────────────────────────
        // STAGE 5, 6 & 7: Persistence of MatchedEmergencyDonor & Notification Dispatch
        // ─────────────────────────────────────────────────────────────────────────────
        persistAndNotifyMatchedDonors(request, hospital, "GROUP_A", groupADonors);
        persistAndNotifyMatchedDonors(request, hospital, "GROUP_B", groupBDonors);
        persistAndNotifyMatchedDonors(request, hospital, "GROUP_C", groupCDonors);
        persistAndNotifyMatchedDonors(request, hospital, "GROUP_D", groupDDonors);

        DonorTierGroupDTO tierGroupA = buildTierGroupDTO(
                "Group A", "Immediate Matches", "0 - 50 KM",
                "🚨 URGENT Blood Request Near You",
                "You are one of the nearest compatible donors. Immediate response requested.",
                groupADonors, 85.0
        );

        DonorTierGroupDTO tierGroupB = buildTierGroupDTO(
                "Group B", "Nearby Compatible", "50 - 75 KM",
                "Blood Donation Request",
                "A nearby hospital requires your blood group. Please respond if available.",
                groupBDonors, 65.0
        );

        DonorTierGroupDTO tierGroupC = buildTierGroupDTO(
                "Group C", "Extended Compatible", "75 - 100 KM",
                "Emergency Blood Donation Needed",
                "A compatible patient urgently needs blood. Although you are farther away, your donation may save a life.",
                groupCDonors, 45.0
        );

        DonorTierGroupDTO tierGroupD = buildTierGroupDTO(
                "Group D", "Emergency Broadcast", "> 100 KM",
                "Critical Blood Emergency",
                "Current nearby donors are insufficient. You are receiving this notification because your blood group is compatible.",
                groupDDonors, 25.0
        );

        long durationMs = System.currentTimeMillis() - startTimeMs;

        // ─────────────────────────────────────────────────────────────────────────────
        // STAGE 8: Analytics Generation & Persistence
        // ─────────────────────────────────────────────────────────────────────────────
        MatchingAnalytics analytics = MatchingAnalytics.builder()
                .bloodRequestId(request.getId())
                .hospitalId(hospital.getId())
                .bloodGroup(neededGroup.name())
                .compatibleDonorsFound(compatibleCount)
                .eligibleDonorsCount(eligibleCount)
                .filteredDonorsCount(filteredCount)
                .groupADonorsCount(groupADonors.size())
                .groupBDonorsCount(groupBDonors.size())
                .groupCDonorsCount(groupCDonors.size())
                .groupDDonorsCount(groupDDonors.size())
                .notificationBatchesSent(1)
                .responseRate(0.0)
                .averageResponseTimeSeconds(0.0)
                .matchingDurationMs(durationMs)
                .build();

        try {
            matchingAnalyticsRepository.save(analytics);
            log.info("[STAGE-8-ANALYTICS] Persisted MatchingAnalytics ID #{} in {} ms", analytics.getId(), durationMs);
        } catch (Exception e) {
            log.error("[STAGE-8-ANALYTICS-ERROR] Failed to persist analytics: {}", e.getMessage(), e);
        }

        SmartDonorMatchingPipelineDTO result = SmartDonorMatchingPipelineDTO.builder()
                .bloodRequestId(request.getId())
                .hospitalId(hospital.getId())
                .bloodGroupNeeded(neededGroup)
                .unitsRequired(request.getUnitsRequired())
                .groupA(tierGroupA)
                .groupB(tierGroupB)
                .groupC(tierGroupC)
                .groupD(tierGroupD)
                .totalCompatibleDonors(compatibleCount)
                .totalEligibleDonors(eligibleCount)
                .totalFilteredDonors(filteredCount)
                .matchingDurationMs(durationMs)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("[SMART-PIPELINE-COMPLETE] Completed evaluation & donor assignment for Blood Request #{} in {} ms", request.getId(), durationMs);
        log.info("================================================================================");

        return result;
    }

    private void persistAndNotifyMatchedDonors(BloodRequest request, Hospital hospital, String groupName, List<DonorMatchSummaryDTO> donorSummaries) {
        if (donorSummaries == null || donorSummaries.isEmpty()) return;

        String bgName = request.getBloodGroupNeeded() != null ? request.getBloodGroupNeeded().name().replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "";
        String title = String.format("Emergency %s blood needed at %s", bgName, hospital.getHospitalName());
        String message = String.format("Urgent requirement for %d units of %s blood at %s.", request.getUnitsRequired(), bgName, hospital.getHospitalName());
        String actionUrl = "/donor/requests";

        for (DonorMatchSummaryDTO summary : donorSummaries) {
            try {
                Optional<DonorProfile> donorOpt = donorProfileRepository.findById(summary.getDonorId());
                if (donorOpt.isEmpty()) continue;
                DonorProfile donor = donorOpt.get();

                MatchedEmergencyDonor matchedDonor = matchedEmergencyDonorRepository
                        .findByBloodRequestIdAndDonorId(request.getId(), donor.getId())
                        .orElseGet(() -> MatchedEmergencyDonor.builder()
                                .bloodRequest(request)
                                .donor(donor)
                                .hospital(hospital)
                                .distanceKm(summary.getDistanceKm())
                                .matchingGroup(groupName)
                                .notificationSent(true)
                                .status(MatchedEmergencyDonorStatus.PENDING)
                                .build());

                matchedDonor.setDistanceKm(summary.getDistanceKm());
                matchedDonor.setMatchingGroup(groupName);
                matchedDonor.setNotificationSent(true);
                MatchedEmergencyDonor savedMatchedDonor = matchedEmergencyDonorRepository.save(matchedDonor);
                log.info("[STAGE-6-PERSIST-SUCCESS] Saved MatchedEmergencyDonor record ID #{} for Blood Request #{} and Donor #{} (Group {}, Distance {} KM)",
                        savedMatchedDonor.getId(), request.getId(), donor.getId(), groupName, summary.getDistanceKm());

                if (notificationService != null) {
                    notificationService.notifyDonor(donor, title, message, NotificationType.EMERGENCY_BLOOD_REQUEST, actionUrl, request, hospital);
                    log.info("[STAGE-7-NOTIFY-SUCCESS] In-app notification generated for Donor #{}", donor.getId());
                }

                if (realtimeService != null) {
                    RealtimeEventDTO emergencyEvent = RealtimeEventDTO.of(
                            RealtimeEventType.EMERGENCY_REQUEST_ALERT,
                            "BLOOD_REQUEST",
                            request.getId(),
                            title,
                            message,
                            request
                    );
                    realtimeService.publishDonorUpdate(donor.getId(), emergencyEvent);

                    RealtimeEventDTO matchedEvent = RealtimeEventDTO.builder()
                            .eventType(RealtimeEventType.DONOR_MATCHED)
                            .requestId(request.getId())
                            .matchedDonorId(savedMatchedDonor.getId())
                            .donorId(donor.getId())
                            .donorName(donor.getUser() != null ? donor.getUser().getFullName() : "Donor")
                            .hospitalId(hospital != null ? hospital.getId() : null)
                            .hospitalName(hospital != null ? hospital.getHospitalName() : null)
                            .bloodGroup(request.getBloodGroupNeeded() != null ? request.getBloodGroupNeeded().name() : null)
                            .status("MATCHED")
                            .timestamp(LocalDateTime.now())
                            .build();
                    realtimeService.publishEmergencyEvent(matchedEvent);
                }

                if (notificationOrchestrator != null) {
                    User donorUser = donor.getUser();
                    String recipientEmail = donorUser != null ? donorUser.getEmail() : donor.getEmail();
                    NotificationPayload payload = NotificationPayload.builder()
                            .emergencyRequestId(request.getId())
                            .recipientUser(donorUser)
                            .recipientDonor(donor)
                            .hospital(hospital)
                            .bloodRequest(request)
                            .recipientEmail(recipientEmail)
                            .title(title)
                            .message(message)
                            .notificationType(NotificationType.EMERGENCY_BLOOD_REQUEST)
                            .priority("HIGH")
                            .actionUrl(actionUrl)
                            .extraData(Map.of("distanceKm", summary.getDistanceKm(), "matchingGroup", groupName))
                            .build();
                    try {
                        notificationOrchestrator.dispatchNotification(payload);
                        log.info("[STAGE-7-ORCHESTRATOR-SUCCESS] Parallel multi-channel notification dispatched for Donor #{}", donor.getId());
                    } catch (Exception e) {
                        log.error("[ORCHESTRATOR-DISPATCH-ERROR] Failed to dispatch via orchestrator for donor #{}: {}", donor.getId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("[PERSIST-NOTIFY-MATCHED-ERROR] Error processing matched donor #{}: {}", summary.getDonorId(), e.getMessage(), e);
            }
        }
    }

    @Override
    @Transactional
    public ApiResponse<SmartDonorMatchingPipelineDTO> executePipelineForRequestId(Long bloodRequestId) {
        BloodRequest request = bloodRequestRepository.findById(bloodRequestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + bloodRequestId));
        SmartDonorMatchingPipelineDTO dto = executePipeline(request);
        return ApiResponse.success("Smart Donor Matching Pipeline executed successfully", dto);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<SmartDonorMatchingPipelineDTO> getPipelineResults(Long bloodRequestId) {
        return executePipelineForRequestId(bloodRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<MatchingAnalytics> getAnalyticsForRequestId(Long bloodRequestId) {
        MatchingAnalytics analytics = matchingAnalyticsRepository.findTopByBloodRequestIdOrderByCreatedAtDesc(bloodRequestId)
                .orElseGet(() -> MatchingAnalytics.builder()
                        .bloodRequestId(bloodRequestId)
                        .bloodGroup("N/A")
                        .build());
        return ApiResponse.success("Matching analytics retrieved successfully", analytics);
    }

    private void validateBloodRequest(BloodRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Blood request cannot be null");
        }
        if (request.getBloodGroupNeeded() == null) {
            throw new IllegalArgumentException("Blood group needed is required");
        }
        if (request.getUnitsRequired() == null || request.getUnitsRequired() <= 0) {
            throw new IllegalArgumentException("Units required must be greater than zero");
        }
        if (request.getHospital() == null) {
            throw new IllegalStateException("Hospital reference is missing for blood request #" + request.getId());
        }
        if (!request.getHospital().isApprovedOrVerified()) {
            throw new IllegalStateException("Hospital #" + request.getHospital().getId() + " is pending approval or unverified");
        }
    }

    private DonorMatchSummaryDTO toSummaryDTO(DonorProfile donor, double distanceKm) {
        User user = donor.getUser();
        return DonorMatchSummaryDTO.builder()
                .donorId(donor.getId())
                .fullName(user != null ? user.getFullName() : "Donor #" + donor.getId())
                .email(user != null ? user.getEmail() : donor.getEmail())
                .phoneNumber(user != null ? user.getPhoneNumber() : null)
                .bloodGroup(donor.getBloodGroup())
                .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
                .lastDonationDate(donor.getLastDonationDate())
                .donorScore(donor.getDonorScore() != null ? donor.getDonorScore().doubleValue() : 100.0)
                .city(donor.getCity())
                .state(donor.getState())
                .latitude(donor.getLatitude())
                .longitude(donor.getLongitude())
                .build();
    }

    private DonorTierGroupDTO buildTierGroupDTO(
            String groupName,
            String sectionTitle,
            String distanceRange,
            String subjectTemplate,
            String messageTemplate,
            List<DonorMatchSummaryDTO> donors,
            double baseProbability
    ) {
        double avgDist = donors.isEmpty() ? 0.0 : donors.stream().mapToDouble(DonorMatchSummaryDTO::getDistanceKm).average().orElse(0.0);

        return DonorTierGroupDTO.builder()
                .groupName(groupName)
                .sectionTitle(sectionTitle)
                .distanceRange(distanceRange)
                .donorCount(donors.size())
                .averageDistanceKm(Math.round(avgDist * 100.0) / 100.0)
                .estimatedResponseProbability(baseProbability)
                .acceptedCount(0)
                .pendingCount(donors.size())
                .notificationSubjectTemplate(subjectTemplate)
                .notificationMessageTemplate(messageTemplate)
                .donors(donors)
                .build();
    }
}
