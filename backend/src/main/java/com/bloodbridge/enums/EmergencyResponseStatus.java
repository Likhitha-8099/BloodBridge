package com.bloodbridge.enums;

/**
 * Enum representing donor response & journey statuses for emergency blood requests.
 */
public enum EmergencyResponseStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    EXPIRED,
    CANCELLED,
    STARTED_TRAVEL,
    REACHED_HOSPITAL,
    DONATION_COMPLETED,
    REWARD_GENERATED,
    JOURNEY_CLOSED
}
