package com.bloodbridge.exception;

/**
 * Custom exception thrown when a requested donor profile cannot be found.
 */
public class DonorProfileNotFoundException extends RuntimeException {

    /**
     * Constructs a new DonorProfileNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public DonorProfileNotFoundException(String message) {
        super(message);
    }
}
