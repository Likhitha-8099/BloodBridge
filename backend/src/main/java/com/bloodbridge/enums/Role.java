package com.bloodbridge.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Enumeration representing the different user roles in the Blood Bridge system.
 */
public enum Role {
    /**
     * System administrator with full access to manage the platform.
     */
    ADMIN,

    /**
     * Individual who registers to donate blood.
     */
    DONOR,

    /**
     * Individual requesting blood donations.
     */
    PATIENT,

    /**
     * Healthcare facility interacting with the platform.
     */
    HOSPITAL;

    @JsonCreator
    public static Role fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Role.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role value: " + value + ". Allowed roles: DONOR, PATIENT, HOSPITAL, ADMIN");
        }
    }
}
