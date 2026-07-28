package com.bloodbridge.exception;

/**
 * Custom exception thrown when a requested hospital profile cannot be found.
 */
public class HospitalNotFoundException extends RuntimeException {

    /**
     * Constructs a new HospitalNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public HospitalNotFoundException(String message) {
        super(message);
    }
}
