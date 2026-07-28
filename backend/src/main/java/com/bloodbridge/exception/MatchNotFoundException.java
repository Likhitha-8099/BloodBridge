package com.bloodbridge.exception;

/**
 * Custom exception thrown when a requested match result cannot be found.
 */
public class MatchNotFoundException extends RuntimeException {

    /**
     * Constructs a new MatchNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public MatchNotFoundException(String message) {
        super(message);
    }
}
