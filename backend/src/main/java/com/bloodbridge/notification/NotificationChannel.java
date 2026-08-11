package com.bloodbridge.notification;

import com.bloodbridge.enums.DeliveryChannel;

/**
 * Strategy interface for notification channels (Email, WebSocket, FCM, SMS, WhatsApp).
 */
public interface NotificationChannel {

    /**
     * Returns the delivery channel type.
     */
    DeliveryChannel getChannel();

    /**
     * Checks if the channel is enabled in current environment configuration.
     */
    boolean isEnabled();

    /**
     * Dispatches notification payload over the channel strategy.
     * @return true if successful, false otherwise.
     */
    boolean send(NotificationPayload payload);
}
