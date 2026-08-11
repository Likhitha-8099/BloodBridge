package com.bloodbridge.service;

/**
 * Service interface for automated emergency request radius escalation.
 */
public interface EmergencyEscalationService {

    /**
     * Periodically checks unfulfilled emergency requests > 5 minutes old and expands radius (50 KM -> 75 KM -> 100 KM).
     */
    void evaluateAndEscalateEmergencies();
}
