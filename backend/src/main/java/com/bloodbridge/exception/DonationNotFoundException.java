package com.bloodbridge.exception;

/**
 * Custom exception thrown when a requested donation record cannot be found.
 */
public class DonationNotFoundException extends RuntimeException {

    /**
     * Constructs a new DonationNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public DonationNotFoundException(String message) {
        super(message);
    }
}
