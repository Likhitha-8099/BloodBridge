package com.bloodbridge.enums;

/**
 * Enumeration representing the delivery status of a notification.
 */
public enum NotificationStatus {
    /**
     * Notification is registered and pending delivery.
     */
    PENDING,

    /**
     * Notification was successfully sent.
     */
    SENT,

    /**
     * Notification delivery failed.
     */
    FAILED
}
