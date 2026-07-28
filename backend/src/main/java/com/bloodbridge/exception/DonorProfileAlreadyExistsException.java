package com.bloodbridge.exception;

/**
 * Custom exception thrown when a user attempts to create a donor profile
 * but one already exists for their account.
 */
public class DonorProfileAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new DonorProfileAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message
     */
    public DonorProfileAlreadyExistsException(String message) {
        super(message);
    }
}
