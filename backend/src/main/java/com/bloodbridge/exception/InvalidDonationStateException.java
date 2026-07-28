package com.bloodbridge.exception;

/**
 * Custom exception thrown when a donation transaction is in an invalid state
 * for the requested operation (e.g. attempting to confirm a completed donation).
 */
public class InvalidDonationStateException extends RuntimeException {

    /**
     * Constructs a new InvalidDonationStateException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidDonationStateException(String message) {
        super(message);
    }
}
