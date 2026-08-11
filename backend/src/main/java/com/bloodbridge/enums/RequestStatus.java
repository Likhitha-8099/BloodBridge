package com.bloodbridge.enums;

/**
 * Enumeration representing the possible states of a blood request in the system.
 */
public enum RequestStatus {
    CREATED,
    PENDING,
    ACTIVE,
    VERIFIED,
    MATCHING,
    MATCHED,
    DONOR_NOTIFIED,
    DONOR_ACCEPTED,
    FULFILLMENT_IN_PROGRESS,
    IN_PROGRESS,
    FULFILLED,
    COMPLETED,
    CANCELLED,
    REJECTED,
    EXPIRED
}
