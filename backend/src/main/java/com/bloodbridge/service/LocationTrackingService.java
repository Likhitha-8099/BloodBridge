package com.bloodbridge.service;

import com.bloodbridge.dto.request.LocationUpdateDTO;
import com.bloodbridge.dto.response.DonorLiveLocationDTO;
import com.bloodbridge.dto.response.TrackingAnalyticsDTO;

import java.util.List;

/**
 * Service interface for real-time donor GPS tracking operations.
 * Handles GPS ingestion, smart update filtering, ETA computation,
 * WebSocket broadcasts, and auto-arrival detection.
 */
public interface LocationTrackingService {

    /**
     * Processes an inbound GPS update from a donor device.
     * Applies smart filtering (accuracy, min-distance, min-interval) before persisting.
     * Computes distance/ETA. Broadcasts the new location over WebSocket.
     * Auto-triggers reachHospital() when donor is within 100m of hospital.
     *
     * @param donorEmail authenticated donor email from JWT
     * @param dto inbound GPS payload
     * @return full live-location DTO with computed fields
     */
    DonorLiveLocationDTO processLocationUpdate(String donorEmail, LocationUpdateDTO dto);

    /**
     * Returns the most-recent live location record for an emergency request (all donor tracking).
     * Used by the hospital dashboard REST endpoint.
     */
    List<DonorLiveLocationDTO> getLiveLocationsForRequest(Long requestId);

    /**
     * Returns full chronological route history for a single donor responding to a request.
     * Used for route playback on the hospital dashboard.
     */
    List<DonorLiveLocationDTO> getRouteHistory(Long requestId, Long donorId);

    /**
     * Returns admin-level telemetry statistics for the live tracking engine.
     */
    TrackingAnalyticsDTO getTrackingAnalytics();
}
