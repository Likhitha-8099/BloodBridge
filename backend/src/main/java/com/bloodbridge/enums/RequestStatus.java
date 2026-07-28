package com.bloodbridge.enums;

/**
 * Enumeration representing the possible states of a blood request in the system.
 */
public enum RequestStatus {
    /**
     * Request is registered and awaiting hospital verification.
     */
    PENDING,

    /**
     * Request has been verified by the assigned hospital.
     */
    VERIFIED,

    /**
     * Request has been matched with eligible donors.
     */
    MATCHED,

    /**
     * Request has been successfully fulfilled and closed.
     */
    COMPLETED,

    /**
     * Request was cancelled by the patient.
     */
    CANCELLED,

    /**
     * Request was rejected by the assigned hospital.
     */
    REJECTED
}
