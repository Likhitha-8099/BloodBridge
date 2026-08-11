package com.bloodbridge.enums;

/**
 * Enumeration representing donor eligibility states.
 */
public enum EligibilityStatus {
    /**
     * Donor meets all health, age, weight, and donation interval criteria.
     */
    ELIGIBLE,

    /**
     * Donor is temporarily deferred due to recent donation, weight, age, or medical recovery.
     */
    TEMPORARILY_DEFERRED,

    /**
     * Donor is permanently deferred due to chronic medical restrictions.
     */
    PERMANENTLY_DEFERRED
}
