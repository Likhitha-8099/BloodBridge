package com.bloodbridge.exception;

/**
 * Custom exception thrown when a donor's weight is below the minimum eligibility threshold (50 kg).
 */
public class InvalidWeightException extends RuntimeException {

    /**
     * Constructs a new InvalidWeightException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidWeightException(String message) {
        super(message);
    }
}
