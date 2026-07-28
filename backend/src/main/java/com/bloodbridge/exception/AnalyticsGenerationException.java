package com.bloodbridge.exception;

/**
 * Custom exception thrown when analytics generation or trend compiling fails.
 */
public class AnalyticsGenerationException extends RuntimeException {

    /**
     * Constructs a new AnalyticsGenerationException with the specified detail message.
     *
     * @param message the detail message
     */
    public AnalyticsGenerationException(String message) {
        super(message);
    }
}
