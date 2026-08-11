package com.bloodbridge.service.impl;

import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.enums.EmergencyResponseStatus;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.notification.NotificationOrchestrator;
import com.bloodbridge.notification.NotificationPayload;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.EmergencyResponseRepository;
import com.bloodbridge.service.DonorMatchingService;
import com.bloodbridge.service.EmergencyEscalationService;
import com.bloodbridge.service.EmergencyTimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of EmergencyEscalationService automating radius expansion (50 KM -> 75 KM -> 100 KM).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmergencyEscalationServiceImpl implements EmergencyEscalationService {

    private final BloodRequestRepository bloodRequestRepository;
    private final EmergencyResponseRepository emergencyResponseRepository;
    private final DonorMatchingService donorMatchingService;
    private final NotificationOrchestrator notificationOrchestrator;
    private final EmergencyTimelineService timelineService;

    @Override
    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    @Transactional
    public void evaluateAndEscalateEmergencies() {
        LocalDateTime now = LocalDateTime.now();
        List<BloodRequest> activeRequests = bloodRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING)
                .toList();

        for (BloodRequest req : activeRequests) {
            if (req.getCreatedAt() == null) continue;
            long elapsedMinutes = Duration.between(req.getCreatedAt(), now).toMinutes();

            if (elapsedMinutes >= 5) {
                long accepted = emergencyResponseRepository.countByBloodRequestIdAndStatus(req.getId(), EmergencyResponseStatus.ACCEPTED);
                if (accepted < req.getUnitsRequired()) {
                    log.info("================================================================================");
                    log.info("[AUTO-ESCALATION] Emergency Request #{} elapsed {} mins with {}/{} accepted donors",
                            req.getId(), elapsedMinutes, accepted, req.getUnitsRequired());

                    // Expand radius to 75 KM or 100 KM
                    double escalatedRadius = elapsedMinutes >= 10 ? 100.0 : 75.0;

                    List<DonorProfile> additionalDonors = donorMatchingService.evaluateEligibleDonors(req).getMatchedDonors();
                    log.info("[AUTO-ESCALATION] Radius expanded to {} KM. Found {} potential donors",
                            escalatedRadius, additionalDonors != null ? additionalDonors.size() : 0);

                    if (additionalDonors != null) {
                        for (DonorProfile donor : additionalDonors) {
                            NotificationPayload payload = NotificationPayload.builder()
                                    .emergencyRequestId(req.getId())
                                    .recipientDonor(donor)
                                    .recipientUser(donor.getUser())
                                    .bloodRequest(req)
                                    .title("🚨 ESCALATED EMERGENCY BLOOD REQUEST")
                                    .message("Emergency search radius has been expanded! Your blood group is urgently needed.")
                                    .build();
                            notificationOrchestrator.dispatchNotification(payload);
                        }
                    }

                    timelineService.recordEvent(req.getId(), "RADIUS_ESCALATED",
                            "Emergency Auto Escalated to " + escalatedRadius + " KM",
                            "Search radius automatically expanded due to unfulfilled units after " + elapsedMinutes + " minutes.",
                            "SYSTEM", String.format("{\"escalatedRadius\": %.1f, \"matchedDonors\": %d}", escalatedRadius, additionalDonors != null ? additionalDonors.size() : 0));

                    log.info("================================================================================");
                }
            }
        }
    }
}
