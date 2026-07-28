package com.bloodbridge.enums;

/**
 * Enumeration representing different types of notifications in the system.
 */
public enum NotificationType {
    /**
     * Triggered when a new blood request is registered.
     */
    BLOOD_REQUEST_CREATED,

    /**
     * Triggered when a blood request is verified by a hospital.
     */
    REQUEST_VERIFIED,

    /**
     * Triggered when a blood request is rejected by a hospital.
     */
    REQUEST_REJECTED,

    /**
     * Triggered when eligible donors are matched for a verified request.
     */
    DONOR_MATCHED,

    /**
     * Triggered when a donor accepts a match request.
     */
    DONATION_ACCEPTED,

    /**
     * Triggered when a hospital confirms a donor.
     */
    DONATION_CONFIRMED,

    /**
     * Triggered when a donation is successfully completed.
     */
    DONATION_COMPLETED,

    /**
     * General system notifications.
     */
    SYSTEM_NOTIFICATION
}
