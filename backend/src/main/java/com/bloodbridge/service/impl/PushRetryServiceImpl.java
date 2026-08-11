package com.bloodbridge.service.impl;

import com.bloodbridge.repository.DeviceTokenRepository;
import com.bloodbridge.service.PushRetryService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Service implementation for FCM transient retries with exponential backoff
 * and automatic invalid token cleanup.
 * Phase 3B.2 — Enterprise Firebase Push Notification Delivery Engine.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushRetryServiceImpl implements PushRetryService {

    private final DeviceTokenRepository deviceTokenRepository;

    private static final Set<String> TRANSIENT_ERRORS = Set.of(
            "UNAVAILABLE",
            "INTERNAL",
            "DEADLINE_EXCEEDED",
            "SERVICE_UNAVAILABLE",
            "RESOURCE_EXHAUSTED",
            "QUOTA_EXCEEDED"
    );

    private static final Set<String> INVALID_TOKEN_ERRORS = Set.of(
            "UNREGISTERED",
            "REGISTRATION_TOKEN_NOT_REGISTERED",
            "INVALID_ARGUMENT",
            "NOT_FOUND",
            "SENDER_ID_MISMATCH"
    );

    private static final long[] BACKOFF_DELAYS_MS = {0L, 1000L, 2000L, 4000L}; // Attempts 1 to 4

    @Override
    public boolean isTransientError(String errorCode) {
        if (errorCode == null) return false;
        String upperCode = errorCode.toUpperCase();
        return TRANSIENT_ERRORS.stream().anyMatch(upperCode::contains);
    }

    @Override
    public String sendWithRetry(Message message, String fcmToken) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("[PUSH-RETRY] FirebaseApp is not initialized. Skipping push send.");
            return null;
        }

        FirebaseMessaging messaging = FirebaseMessaging.getInstance();
        String lastErrorMsg = null;

        for (int attempt = 1; attempt <= BACKOFF_DELAYS_MS.length; attempt++) {
            long delay = BACKOFF_DELAYS_MS[attempt - 1];
            if (delay > 0) {
                try {
                    log.info("[PUSH-RETRY] Exponential backoff attempt #{}/{} after {} ms for token: {}...",
                            attempt, BACKOFF_DELAYS_MS.length, delay, fcmToken.substring(0, Math.min(fcmToken.length(), 15)));
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("[PUSH-RETRY] Interrupted during backoff sleep");
                    break;
                }
            }

            try {
                String messageId = messaging.send(message);
                log.info("[PUSH-RETRY-SUCCESS] Message delivered on attempt #{}. Message ID: {}", attempt, messageId);
                return messageId;
            } catch (FirebaseMessagingException fme) {
                String errorCode = fme.getErrorCode() != null ? fme.getErrorCode().name() : fme.getMessage();
                lastErrorMsg = errorCode;
                log.warn("[PUSH-RETRY] Attempt #{}/{} failed for token (Error: {})", attempt, BACKOFF_DELAYS_MS.length, errorCode);

                if (isInvalidTokenError(errorCode)) {
                    log.info("[PUSH-CLEANUP] Permanent token error detected: {}. Deactivating token...", errorCode);
                    handleInvalidToken(fcmToken, errorCode);
                    return null; // Do not retry permanent errors
                }

                if (!isTransientError(errorCode)) {
                    log.warn("[PUSH-RETRY] Non-transient error detected ({}), aborting retry loop.", errorCode);
                    return null;
                }
            } catch (Exception e) {
                lastErrorMsg = e.getMessage();
                log.error("[PUSH-RETRY] Unexpected exception on attempt #{}: {}", attempt, e.getMessage());
                if (!isTransientError(e.getMessage())) {
                    break;
                }
            }
        }

        log.error("[PUSH-RETRY-EXHAUSTED] All {} retry attempts failed for token. Final error: {}",
                BACKOFF_DELAYS_MS.length, lastErrorMsg);
        return null;
    }

    @Override
    @Transactional
    public void handleInvalidToken(String fcmToken, String reason) {
        if (fcmToken == null || fcmToken.isBlank()) return;
        try {
            log.info("[INVALID-TOKEN-CLEANUP] Deactivating invalid FCM token in database. Reason: {}", reason);
            deviceTokenRepository.deleteByFcmToken(fcmToken);
            log.info("[INVALID-TOKEN-CLEANUP] ✔ Successfully removed invalid token from database.");
        } catch (Exception e) {
            log.error("[INVALID-TOKEN-CLEANUP-ERROR] Failed to remove invalid token from database: {}", e.getMessage(), e);
        }
    }

    private boolean isInvalidTokenError(String errorCode) {
        if (errorCode == null) return false;
        String upperCode = errorCode.toUpperCase();
        return INVALID_TOKEN_ERRORS.stream().anyMatch(upperCode::contains);
    }
}
