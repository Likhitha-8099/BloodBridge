package com.bloodbridge.enums;

/**
 * Enumeration representing the states of a blood matching result.
 */
public enum MatchStatus {
    /**
     * Match has been generated and is pending donor/patient acceptance.
     */
    MATCHED,

    /**
     * Match was accepted by the donor.
     */
    ACCEPTED,

    /**
     * Match was rejected.
     */
    REJECTED,

    /**
     * Match expired.
     */
    EXPIRED
}
