package com.bloodbridge.exception;

/**
 * Custom exception thrown when a user registration is attempted with an email that already exists.
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    /**
     * Constructs a new UserAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
