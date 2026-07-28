package com.bloodbridge.exception;

/**
 * Custom exception thrown when a duplicate donation record creation is attempted
 * (e.g. donor attempts to accept the same request twice).
 */
public class DuplicateDonationException extends RuntimeException {

    /**
     * Constructs a new DuplicateDonationException with the specified detail message.
     *
     * @param message the detail message
     */
    public DuplicateDonationException(String message) {
        super(message);
    }
}
