package com.bloodbridge.exception;

/**
 * Custom exception thrown when email dispatch operations fail.
 */
public class EmailDeliveryException extends RuntimeException {

    /**
     * Constructs a new EmailDeliveryException with the specified detail message.
     *
     * @param message the detail message
     */
    public EmailDeliveryException(String message) {
        super(message);
    }

    /**
     * Constructs a new EmailDeliveryException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
