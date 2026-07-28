package com.bloodbridge.exception;

/**
 * Custom exception thrown when a user attempts to create a hospital profile
 * but one already exists for their account.
 */
public class HospitalAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new HospitalAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message
     */
    public HospitalAlreadyExistsException(String message) {
        super(message);
    }
}
