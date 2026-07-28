package com.bloodbridge.exception;

/**
 * Custom exception thrown when a requested patient profile cannot be found.
 */
public class PatientProfileNotFoundException extends RuntimeException {

    /**
     * Constructs a new PatientProfileNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public PatientProfileNotFoundException(String message) {
        super(message);
    }
}
