package com.bloodbridge.enums;

/**
 * Enumeration representing the possible states of a donation tracking record.
 */
public enum DonationStatus {
    /**
     * Donation request is pending and awaiting initial action.
     */
    PENDING,

    /**
     * Donor accepted the matching request.
     */
    ACCEPTED,

    /**
     * Donor rejected the matching request.
     */
    REJECTED,

    /**
     * Assigned hospital confirmed the donor and scheduled donation.
     */
    CONFIRMED,

    /**
     * Donation was successfully completed, and donor stats were updated.
     */
    COMPLETED,

    /**
     * Donation transaction was cancelled.
     */
    CANCELLED
}
