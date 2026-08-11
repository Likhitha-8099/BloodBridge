package com.bloodbridge.event;

import com.bloodbridge.dto.RealtimeEventDTO;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.enums.RealtimeEventType;
import com.bloodbridge.service.AuditLoggerService;
import com.bloodbridge.service.NotificationService;
import com.bloodbridge.service.RealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event Listener handling post-commit side-effects for hospital verification events.
 * Executes AFTER transaction commit to ensure database consistency is never compromised by secondary operations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HospitalVerificationEventListener {

    private final AuditLoggerService auditLoggerService;
    private final RealtimeService realtimeService;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleHospitalVerificationEvent(HospitalVerificationEvent event) {
        log.info("[AFTER-COMMIT] Processing post-commit side effects for Hospital ID: {} (status={})",
                event.getHospital().getId(), event.getStatus());

        // 1. Audit Logging
        try {
            auditLoggerService.logEvent(
                    event.isApproved() ? "HOSPITAL_APPROVED" : "HOSPITAL_REJECTED",
                    event.getAdminEmail(),
                    "Hospital ID " + event.getHospital().getId() + " verified=" + event.getHospital().getVerified() +
                            ", status=" + event.getHospital().getVerificationStatus()
            );
        } catch (Exception e) {
            log.error("[AFTER-COMMIT-ERROR] Failed to log audit event: {}", e.getMessage(), e);
        }

        // 2. Realtime WebSocket STOMP Publishing
        try {
            RealtimeEventType eventType = event.isApproved() ? RealtimeEventType.HOSPITAL_APPROVED : RealtimeEventType.HOSPITAL_REJECTED;
            RealtimeEventDTO dto = RealtimeEventDTO.of(
                    eventType,
                    "HOSPITAL",
                    event.getHospital().getId(),
                    event.isApproved() ? "Hospital Approved" : "Hospital Rejected",
                    String.format("Hospital %s has been %s by admin", event.getHospital().getHospitalName(), event.isApproved() ? "approved" : "rejected"),
                    event.getResponse()
            );

            realtimeService.publishAdminHospitalsUpdate(dto);
            realtimeService.publishAdminDashboardUpdate(dto);
            realtimeService.publishHospitalUpdate(event.getHospital().getId(), dto);
        } catch (Exception e) {
            log.error("[AFTER-COMMIT-ERROR] Failed to publish WebSocket event: {}", e.getMessage(), e);
        }

        // 3. Notification Dispatch
        if (event.getHospital().getUser() != null) {
            try {
                notificationService.triggerNotificationEvent(
                        event.getHospital().getUser(),
                        event.isApproved() ? "Hospital Registration Approved!" : "Hospital Registration Rejected",
                        event.isApproved()
                                ? "Congratulations! Your hospital registration has been approved by administrator (" + event.getAdminEmail() + "). You can now log in and access the Hospital Dashboard."
                                : "Your hospital registration request was rejected by administrator (" + event.getAdminEmail() + "). Reason: " + (event.getRemarks() != null ? event.getRemarks() : "License or registration details non-compliant"),
                        NotificationType.HOSPITAL_APPROVAL,
                        DeliveryChannel.IN_APP,
                        "HIGH"
                );
            } catch (Exception e) {
                log.error("[AFTER-COMMIT-ERROR] Failed to trigger notification event: {}", e.getMessage(), e);
            }
        }
    }
}
