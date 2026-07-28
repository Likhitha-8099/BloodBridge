package com.bloodbridge.exception;

/**
 * Custom exception thrown when a user attempts to create a patient profile
 * but one already exists for their account.
 */
public class PatientProfileAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new PatientProfileAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message
     */
    public PatientProfileAlreadyExistsException(String message) {
        super(message);
    }
}
