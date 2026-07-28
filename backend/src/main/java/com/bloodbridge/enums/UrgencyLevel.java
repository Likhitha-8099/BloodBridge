package com.bloodbridge.enums;

/**
 * Enumeration representing the urgency levels of blood requests in the system.
 */
public enum UrgencyLevel {
    /**
     * Low urgency request (normal priority).
     */
    LOW,

    /**
     * Medium urgency request.
     */
    MEDIUM,

    /**
     * High urgency request (fast resolution needed).
     */
    HIGH,

    /**
     * Critical urgency request (life-threatening, immediate action required).
     */
    CRITICAL
}
