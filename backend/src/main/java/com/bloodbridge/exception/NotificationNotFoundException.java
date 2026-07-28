package com.bloodbridge.exception;

/**
 * Custom exception thrown when a requested notification cannot be found.
 */
public class NotificationNotFoundException extends RuntimeException {

    /**
     * Constructs a new NotificationNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public NotificationNotFoundException(String message) {
        super(message);
    }
}
