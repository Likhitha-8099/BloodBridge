package com.bloodbridge.service;

import com.google.firebase.messaging.Message;

/**
 * Service interface for handling Firebase transient retries with exponential backoff
 * and automatic invalid token cleanup.
 * Phase 3B.2 — Enterprise Firebase Push Notification Delivery Engine.
 */
public interface PushRetryService {

    /**
     * Determines whether an FCM error code/exception is transient and eligible for retry.
     *
     * @param errorCode FCM error code string or exception message
     * @return true if transient, false if permanent
     */
    boolean isTransientError(String errorCode);

    /**
     * Executes a single message dispatch with retry logic.
     *
     * @param message  FCM Message
     * @param fcmToken FCM token string
     * @return Message ID on success, or null on permanent failure
     */
    String sendWithRetry(Message message, String fcmToken);

    /**
     * Handles invalid FCM tokens by deactivating them from the database.
     *
     * @param fcmToken FCM token string to deactivate
     * @param reason   Reason string for logging
     */
    void handleInvalidToken(String fcmToken, String reason);
}
