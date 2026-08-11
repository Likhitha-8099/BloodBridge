package com.bloodbridge.notification.channel;

import com.bloodbridge.dto.EmergencyPopupDTO;
import com.bloodbridge.dto.NotificationDTO;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.notification.NotificationChannel;
import com.bloodbridge.notification.NotificationPayload;
import com.bloodbridge.service.RealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy Channel for STOMP WebSocket Real-Time Alert Dispatch.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationChannel implements NotificationChannel {

    private final RealtimeService realtimeService;

    @Override
    public DeliveryChannel getChannel() {
        return DeliveryChannel.IN_APP;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean send(NotificationPayload payload) {
        if (!isEnabled()) return false;

        try {
            if (payload.getRecipientDonor() != null && payload.getBloodRequest() != null) {
                var donor = payload.getRecipientDonor();
                var req = payload.getBloodRequest();
                var hospital = req.getHospital();

                EmergencyPopupDTO popupDTO = EmergencyPopupDTO.builder()
                        .emergencyRequestId(req.getId())
                        .hospitalName(hospital != null ? hospital.getHospitalName() : "Emergency Center")
                        .bloodGroupNeeded(req.getBloodGroupNeeded() != null ? req.getBloodGroupNeeded().name() : "ANY")
                        .unitsRequired(req.getUnitsRequired() != null ? req.getUnitsRequired() : 1)
                        .reason(req.getReason())
                        .urgencyLevel(req.getUrgencyLevel() != null ? req.getUrgencyLevel().name() : "HIGH")
                        .distanceKm(payload.getExtraData() != null && payload.getExtraData().containsKey("distanceKm")
                                ? (Double) payload.getExtraData().get("distanceKm") : 5.0)
                        .hospitalLatitude(hospital != null ? hospital.getLatitude() : null)
                        .hospitalLongitude(hospital != null ? hospital.getLongitude() : null)
                        .build();

                String destination = "/topic/donors/" + donor.getId() + "/emergency-alert";
                realtimeService.publishGlobalEvent(destination, popupDTO);
                log.info("[WEBSOCKET-CHANNEL-SUCCESS] Broadcast popup to donor ID: {}", donor.getId());
            } else if (payload.getRecipientUser() != null) {
                NotificationDTO dto = NotificationDTO.builder()
                        .title(payload.getTitle())
                        .message(payload.getMessage())
                        .notificationType(payload.getNotificationType())
                        .actionUrl(payload.getActionUrl())
                        .build();
                realtimeService.publishUserNotification(payload.getRecipientUser().getId(), dto);
                log.info("[WEBSOCKET-CHANNEL-SUCCESS] User notification sent to user ID: {}", payload.getRecipientUser().getId());
            }
            return true;
        } catch (Exception e) {
            log.error("[WEBSOCKET-CHANNEL-ERROR] Failed dispatch: {}", e.getMessage(), e);
            return false;
        }
    }
}
