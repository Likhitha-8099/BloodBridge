package com.bloodbridge.exception;

/**
 * Custom exception thrown when a donor's age violates eligibility constraints (18-60 years).
 */
public class InvalidAgeException extends RuntimeException {

    /**
     * Constructs a new InvalidAgeException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidAgeException(String message) {
        super(message);
    }
}
