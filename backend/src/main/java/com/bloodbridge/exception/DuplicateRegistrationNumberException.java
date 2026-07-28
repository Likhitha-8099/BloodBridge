package com.bloodbridge.exception;

/**
 * Custom exception thrown when a hospital profile registration or update is attempted
 * using a registration number that already exists in the system.
 */
public class DuplicateRegistrationNumberException extends RuntimeException {

    /**
     * Constructs a new DuplicateRegistrationNumberException with the specified detail message.
     *
     * @param message the detail message
     */
    public DuplicateRegistrationNumberException(String message) {
        super(message);
    }
}
