package com.bloodbridge.exception;

/**
 * Custom exception thrown when retrieval or compilation of dashboard stats fails.
 */
public class DashboardDataException extends RuntimeException {

    /**
     * Constructs a new DashboardDataException with the specified detail message.
     *
     * @param message the detail message
     */
    public DashboardDataException(String message) {
        super(message);
    }
}
