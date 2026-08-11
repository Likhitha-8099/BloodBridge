package com.bloodbridge.enums;

/**
 * Represents the real-time movement state of a donor during an active emergency journey.
 */
public enum TrackingStatus {

    /** Donor has accepted and is about to begin travelling. */
    STARTED,

    /** Donor is actively moving toward the hospital (speed > 0.5 km/h). */
    MOVING,

    /** Donor is temporarily stationary mid-journey (speed ~= 0). */
    STOPPED,

    /** Donor has arrived within 100m of the hospital — auto-arrival triggered. */
    REACHED,

    /** Donation has been completed; tracking session closed. */
    COMPLETED
}
