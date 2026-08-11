package com.bloodbridge.controller;

import com.bloodbridge.dto.request.LocationUpdateDTO;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.DonorLiveLocationDTO;
import com.bloodbridge.dto.response.TrackingAnalyticsDTO;
import com.bloodbridge.service.LocationTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for the real-time GPS donor tracking system.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST  /api/v1/location/update         – Ingest donor GPS telemetry</li>
 *   <li>GET   /api/v1/location/live/{requestId}    – Live map snapshot for a request</li>
 *   <li>GET   /api/v1/location/history/{requestId} – Full route history for a donor</li>
 *   <li>GET   /api/v1/location/analytics      – Admin tracking metrics</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/location")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationTrackingService locationTrackingService;

    /**
     * Ingest a GPS coordinate update from an authenticated donor device.
     * Accepts only donors who have an ACCEPTED or TRAVELLING status for the given blood request.
     */
    @PostMapping("/update")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<ApiResponse<DonorLiveLocationDTO>> updateLocation(
            @Valid @RequestBody LocationUpdateDTO dto,
            Authentication auth) {

        String donorEmail = auth.getName();
        log.info("[LOCATION-CTRL] POST /update from donor={} for requestId={}", donorEmail, dto.getBloodRequestId());

        DonorLiveLocationDTO result = locationTrackingService.processLocationUpdate(donorEmail, dto);
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", result));
    }

    /**
     * Returns the most-recent GPS position for every accepted donor tracking the given blood request.
     * Used by the hospital dashboard to populate the live map.
     */
    @GetMapping("/live/{requestId}")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DonorLiveLocationDTO>>> getLiveLocations(
            @PathVariable Long requestId) {

        log.info("[LOCATION-CTRL] GET /live/{}", requestId);
        List<DonorLiveLocationDTO> locations = locationTrackingService.getLiveLocationsForRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success("Live locations fetched", locations));
    }

    /**
     * Returns the full GPS route history for a specific donor responding to a blood request.
     * Enables route-replay animation on the hospital dashboard.
     */
    @GetMapping("/history/{requestId}")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<List<DonorLiveLocationDTO>>> getRouteHistory(
            @PathVariable Long requestId,
            @RequestParam Long donorId) {

        log.info("[LOCATION-CTRL] GET /history/{} for donorId={}", requestId, donorId);
        List<DonorLiveLocationDTO> history = locationTrackingService.getRouteHistory(requestId, donorId);
        return ResponseEntity.ok(ApiResponse.success("Route history fetched", history));
    }

    /**
     * Admin telemetry endpoint returning aggregated live tracking statistics.
     */
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TrackingAnalyticsDTO>> getTrackingAnalytics() {
        log.info("[LOCATION-CTRL] GET /analytics");
        TrackingAnalyticsDTO analytics = locationTrackingService.getTrackingAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Tracking analytics fetched", analytics));
    }
}
