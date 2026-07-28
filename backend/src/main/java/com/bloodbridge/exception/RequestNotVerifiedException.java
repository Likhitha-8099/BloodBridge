package com.bloodbridge.exception;

/**
 * Custom exception thrown when match generation is attempted
 * on a blood request that is not verified.
 */
public class RequestNotVerifiedException extends RuntimeException {

    /**
     * Constructs a new RequestNotVerifiedException with the specified detail message.
     *
     * @param message the detail message
     */
    public RequestNotVerifiedException(String message) {
        super(message);
    }
}
