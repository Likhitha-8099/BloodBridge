package com.bloodbridge.exception;

/**
 * Custom exception thrown when a blood request is in an invalid state for the
 * requested operation (e.g., trying to edit a completed or cancelled request).
 */
public class InvalidRequestStateException extends RuntimeException {

    /**
     * Constructs a new InvalidRequestStateException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidRequestStateException(String message) {
        super(message);
    }
}
