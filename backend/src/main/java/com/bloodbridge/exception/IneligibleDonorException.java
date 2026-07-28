package com.bloodbridge.exception;

/**
 * Custom exception thrown when matching calculations are requested
 * but a donor fails age, weight, or interval eligibility constraints.
 */
public class IneligibleDonorException extends RuntimeException {

    /**
     * Constructs a new IneligibleDonorException with the specified detail message.
     *
     * @param message the detail message
     */
    public IneligibleDonorException(String message) {
        super(message);
    }
}
