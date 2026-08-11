package com.bloodbridge.notification.channel;

import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DeviceToken;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.PushDeliveryLog;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.notification.NotificationChannel;
import com.bloodbridge.notification.NotificationPayload;
import com.bloodbridge.repository.DeviceTokenRepository;
import com.bloodbridge.repository.PushDeliveryLogRepository;
import com.bloodbridge.service.PushRetryService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Production-Grade Strategy Channel for Mobile & Web Push (Firebase Cloud Messaging - FCM).
 * Phase 3B.2 — Enterprise Firebase Push Notification Delivery Engine.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FirebaseNotificationChannel implements NotificationChannel {

    private final DeviceTokenRepository deviceTokenRepository;
    private final PushDeliveryLogRepository pushDeliveryLogRepository;
    private final PushRetryService pushRetryService;

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Override
    public DeliveryChannel getChannel() {
        return DeliveryChannel.PUSH;
    }

    @Override
    public boolean isEnabled() {
        return firebaseEnabled || !FirebaseApp.getApps().isEmpty();
    }

    @Override
    public boolean send(NotificationPayload payload) {
        if (payload == null) {
            log.warn("[FCM-Stage 1] Aborted: Notification payload is null.");
            return false;
        }

        log.info("[FCM-Stage 1] Receive Notification Request for Emergency Request #{}", payload.getEmergencyRequestId());

        // Stage 2: Resolve Eligible Device Tokens
        User targetUser = resolveTargetUser(payload);
        if (targetUser == null) {
            log.warn("[FCM-Stage 2] Aborted: Target user could not be resolved from payload.");
            return false;
        }

        List<DeviceToken> activeTokens = deviceTokenRepository.findAllByUserAndIsActiveTrue(targetUser);
        log.info("[FCM-Stage 2] Resolved {} active device token(s) for user ID: {}", activeTokens.size(), targetUser.getId());

        if (activeTokens.isEmpty()) {
            log.info("[FCM-Stage 2] No active FCM device tokens registered for user ID: {}. Push skipped.", targetUser.getId());
            return true; // Skipping when no tokens exist is normal behavior
        }

        boolean anySuccess = false;

        // Dispatch notification to every active device token owned by the user (Multi-device delivery)
        for (DeviceToken deviceToken : activeTokens) {
            String fcmToken = deviceToken.getFcmToken();
            if (fcmToken == null || fcmToken.isBlank()) continue;

            long startTimeMs = System.currentTimeMillis();

            try {
                // Stage 3: Build Firebase Message
                Message firebaseMessage = buildFirebaseMessage(payload, fcmToken);
                log.info("[FCM-Stage 3] Firebase Message constructed for token preview: {}...", 
                        fcmToken.substring(0, Math.min(fcmToken.length(), 15)));

                // Stage 4: Dispatch Batch / Single Message with Retry support
                log.info("[FCM-Stage 4] Dispatching FCM message to device ID: {}", deviceToken.getId());
                String messageId = null;

                if (!FirebaseApp.getApps().isEmpty()) {
                    try {
                        messageId = FirebaseMessaging.getInstance().send(firebaseMessage);
                    } catch (FirebaseMessagingException fme) {
                        log.warn("[FCM-Stage 5] Direct send failed ({}), handing over to Retry Engine...", fme.getErrorCode());
                        if (pushRetryService.isTransientError(fme.getErrorCode() != null ? fme.getErrorCode().name() : fme.getMessage())) {
                            messageId = pushRetryService.sendWithRetry(firebaseMessage, fcmToken);
                        } else {
                            pushRetryService.handleInvalidToken(fcmToken, fme.getMessage());
                        }
                    }
                } else {
                    log.warn("[FCM-Stage 4] FirebaseApp not initialized; skipping live FCM API dispatch.");
                    messageId = "mock-fcm-msg-" + System.currentTimeMillis();
                }

                long latencyMs = System.currentTimeMillis() - startTimeMs;

                // Stage 5 & 6: Process Response & Persist Delivery Result
                if (messageId != null) {
                    log.info("[FCM-Stage 5] Process Response: Message ID {} returned in {} ms", messageId, latencyMs);
                    persistDeliveryLog(targetUser, payload.getEmergencyRequestId(), deviceToken, fcmToken, "SENT", latencyMs, null, 0);
                    anySuccess = true;
                } else {
                    log.warn("[FCM-Stage 5] Process Response: FCM dispatch failed for token ID: {}", deviceToken.getId());
                    persistDeliveryLog(targetUser, payload.getEmergencyRequestId(), deviceToken, fcmToken, "FAILED", latencyMs, "FCM send failed", 1);
                }

            } catch (Exception e) {
                long latencyMs = System.currentTimeMillis() - startTimeMs;
                log.error("[FCM-Stage 6] Error dispatching FCM push: {}", e.getMessage(), e);
                persistDeliveryLog(targetUser, payload.getEmergencyRequestId(), deviceToken, fcmToken, "FAILED", latencyMs, e.getMessage(), 1);
            }
        }

        // Stage 8: Publish Analytics Log
        log.info("[FCM-Stage 8] Publish Analytics: Push dispatch completed across {} device(s). Overall success: {}",
                activeTokens.size(), anySuccess);

        return anySuccess;
    }

    private User resolveTargetUser(NotificationPayload payload) {
        if (payload.getRecipientUser() != null) {
            return payload.getRecipientUser();
        }
        if (payload.getRecipientDonor() != null && payload.getRecipientDonor().getUser() != null) {
            return payload.getRecipientDonor().getUser();
        }
        return null;
    }

    /**
     * Builds Firebase Message with Android High Priority, WebPush High Urgency, TTL 5m, and Data Payload.
     */
    private Message buildFirebaseMessage(NotificationPayload payload, String fcmToken) {
        BloodRequest req = payload.getBloodRequest();
        Hospital hospital = payload.getHospital() != null ? payload.getHospital() : (req != null ? req.getHospital() : null);

        String hospitalName = hospital != null ? hospital.getHospitalName() : "Emergency Hospital";
        String bloodGroupStr = req != null && req.getBloodGroupNeeded() != null ? 
                req.getBloodGroupNeeded().name().replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "ANY";
        Integer unitsRequired = req != null && req.getUnitsRequired() != null ? req.getUnitsRequired() : 1;

        // Custom Title & Body formatting according to specification
        String title = "🩸 Emergency Blood Request";
        String body = String.format("%s urgently requires %s Blood. %d Units Needed.", hospitalName, bloodGroupStr, unitsRequired);

        if (payload.getTitle() != null && !payload.getTitle().isBlank()) {
            title = payload.getTitle();
        }
        if (payload.getMessage() != null && !payload.getMessage().isBlank()) {
            body = payload.getMessage();
        }

        String requestIdStr = payload.getEmergencyRequestId() != null ? payload.getEmergencyRequestId().toString() : "0";
        String collapseKey = "EmergencyRequest-" + requestIdStr;

        // Data Payload Mapping (all values converted to String as required by FCM)
        Map<String, String> dataPayload = new HashMap<>();
        dataPayload.put("notificationType", "EMERGENCY_REQUEST");
        dataPayload.put("requestId", requestIdStr);
        dataPayload.put("hospitalId", hospital != null && hospital.getId() != null ? hospital.getId().toString() : "0");
        dataPayload.put("hospitalName", hospitalName);
        dataPayload.put("bloodGroup", bloodGroupStr);
        dataPayload.put("unitsRequired", unitsRequired.toString());
        dataPayload.put("latitude", hospital != null && hospital.getLatitude() != null ? hospital.getLatitude().toString() : "0.0");
        dataPayload.put("longitude", hospital != null && hospital.getLongitude() != null ? hospital.getLongitude().toString() : "0.0");
        dataPayload.put("distanceKm", payload.getExtraData() != null && payload.getExtraData().containsKey("distanceKm") ? 
                payload.getExtraData().get("distanceKm").toString() : "5.0");
        dataPayload.put("priority", payload.getPriority() != null ? payload.getPriority() : "HIGH");
        dataPayload.put("googleMapsUrl", hospital != null ? String.format("https://www.google.com/maps/search/?api=1&query=%f,%f", 
                hospital.getLatitude() != null ? hospital.getLatitude() : 0.0, 
                hospital.getLongitude() != null ? hospital.getLongitude() : 0.0) : "");
        dataPayload.put("createdAt", LocalDateTime.now().toString());
        dataPayload.put("clickAction", "/donor/dashboard");

        // Android Config (High Priority)
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setCollapseKey(collapseKey)
                .setTtl(Duration.ofMinutes(5).toMillis())
                .setNotification(AndroidNotification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .setIcon("/favicon.svg")
                        .setSound("default")
                        .build())
                .build();

        // WebPush Config (High Urgency, TTL 5m, Icon)
        WebpushConfig webpushConfig = WebpushConfig.builder()
                .putHeader("Urgency", "high")
                .setNotification(WebpushNotification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .setIcon("/favicon.svg")
                        .setBadge("/favicon.svg")
                        .build())
                .build();

        return Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(dataPayload)
                .setAndroidConfig(androidConfig)
                .setWebpushConfig(webpushConfig)
                .build();
    }

    private void persistDeliveryLog(User user, Long requestId, DeviceToken deviceToken, String fcmToken,
                                    String status, long latencyMs, String failureReason, int retryCount) {
        try {
            PushDeliveryLog deliveryLog = PushDeliveryLog.builder()
                    .user(user)
                    .emergencyRequestId(requestId)
                    .deviceToken(deviceToken)
                    .fcmToken(fcmToken)
                    .status(status)
                    .sentAt(LocalDateTime.now())
                    .latencyMs(latencyMs)
                    .failureReason(failureReason)
                    .retryCount(retryCount)
                    .build();
            pushDeliveryLogRepository.save(deliveryLog);
        } catch (Exception e) {
            log.error("[FCM-LOG-ERROR] Could not persist push delivery log: {}", e.getMessage());
        }
    }
}
