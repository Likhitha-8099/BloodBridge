package com.bloodbridge.notification;

import java.util.List;

/**
 * Extensible Strategy Pattern Notification Orchestrator Interface.
 */
public interface NotificationOrchestrator {

    /**
     * Dispatches notification across all active channels matching target policy.
     */
    void dispatchNotification(NotificationPayload payload);

    /**
     * Returns list of currently registered notification channels.
     */
    List<NotificationChannel> getRegisteredChannels();
}
