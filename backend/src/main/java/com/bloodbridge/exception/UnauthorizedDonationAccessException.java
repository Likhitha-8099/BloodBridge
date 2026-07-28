package com.bloodbridge.exception;

/**
 * Custom exception thrown when a user attempts to access or modify a donation record
 * without proper role or ownership authorization.
 */
public class UnauthorizedDonationAccessException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedDonationAccessException with the specified detail message.
     *
     * @param message the detail message
     */
    public UnauthorizedDonationAccessException(String message) {
        super(message);
    }
}
