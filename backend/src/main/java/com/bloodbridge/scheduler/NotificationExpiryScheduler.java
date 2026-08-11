package com.bloodbridge.scheduler;

import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.Notification;
import com.bloodbridge.enums.NotificationStatus;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler for automatic expiration of stale emergency notifications after 24 hours.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationExpiryScheduler {

    private final NotificationRepository notificationRepository;
    private final com.bloodbridge.repository.BloodRequestRepository bloodRequestRepository;
    private final com.bloodbridge.repository.MatchedEmergencyDonorRepository matchedEmergencyDonorRepository;

    @Scheduled(cron = "0 */15 * * * *") // Run every 15 minutes
    @Transactional
    public void expireStaleEmergencyNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Notification> staleNotifs = notificationRepository.findExpiredEmergencyNotifications(cutoff);

        int expiredCount = 0;
        for (Notification notif : staleNotifs) {
            BloodRequest req = notif.getBloodRequest();
            // Expire if emergency request is fulfilled/cancelled or no longer pending/open
            if (req == null || req.getStatus() == RequestStatus.FULFILLED || req.getStatus() == RequestStatus.CANCELLED || req.getStatus() == RequestStatus.EXPIRED || notif.getCreatedAt().isBefore(cutoff)) {
                notif.setStatus(NotificationStatus.FAILED); // or set status EXPIRED
                notif.setExpiryTime(LocalDateTime.now());
                notificationRepository.save(notif);
                expiredCount++;
            }
        }

        if (expiredCount > 0) {
            log.info("[NOTIFICATION-EXPIRY] Expired {} emergency notifications older than 24 hours", expiredCount);
        }

        // Auto-expire blood requests where requiredByDate has passed
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            List<BloodRequest> openRequests = bloodRequestRepository.findByStatusIn(List.of(
                    RequestStatus.CREATED, RequestStatus.PENDING, RequestStatus.ACTIVE,
                    RequestStatus.VERIFIED, RequestStatus.MATCHING, RequestStatus.MATCHED, RequestStatus.DONOR_NOTIFIED
            ));
            int expiredReqCount = 0;
            for (BloodRequest req : openRequests) {
                if (req.getRequiredByDate() != null && req.getRequiredByDate().isBefore(today)) {
                    req.setStatus(RequestStatus.EXPIRED);
                    bloodRequestRepository.save(req);

                    List<com.bloodbridge.entity.MatchedEmergencyDonor> matchedDonors = matchedEmergencyDonorRepository.findByBloodRequestId(req.getId());
                    for (com.bloodbridge.entity.MatchedEmergencyDonor med : matchedDonors) {
                        if (med.getStatus() == com.bloodbridge.enums.MatchedEmergencyDonorStatus.PENDING || med.getStatus() == com.bloodbridge.enums.MatchedEmergencyDonorStatus.VIEWED) {
                            med.setStatus(com.bloodbridge.enums.MatchedEmergencyDonorStatus.EXPIRED);
                            matchedEmergencyDonorRepository.save(med);
                        }
                    }
                    expiredReqCount++;
                }
            }
            if (expiredReqCount > 0) {
                log.info("[REQUEST-EXPIRY] Marked {} past-due emergency blood requests as EXPIRED", expiredReqCount);
            }
        } catch (Exception e) {
            log.error("[REQUEST-EXPIRY-ERROR] Failed to run blood request expiry check: {}", e.getMessage());
        }
    }
}
