package com.bloodbridge.exception;

/**
 * Custom exception thrown when a donor attempts to perform an operation but fails
 * general clinical eligibility criteria (e.g., elapsed time since last donation).
 */
public class EligibilityViolationException extends RuntimeException {

    /**
     * Constructs a new EligibilityViolationException with the specified detail message.
     *
     * @param message the detail message
     */
    public EligibilityViolationException(String message) {
        super(message);
    }
}
