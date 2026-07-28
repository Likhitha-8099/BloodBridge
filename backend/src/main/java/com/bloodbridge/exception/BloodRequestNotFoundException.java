package com.bloodbridge.exception;

/**
 * Custom exception thrown when a requested blood request profile cannot be found.
 */
public class BloodRequestNotFoundException extends RuntimeException {

    /**
     * Constructs a new BloodRequestNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public BloodRequestNotFoundException(String message) {
        super(message);
    }
}
