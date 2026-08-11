package com.bloodbridge.dto.response;

import com.bloodbridge.enums.TrackingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Outbound DTO representing the complete live state of a donor's journey toward a hospital.
 * This is broadcast over WebSocket and also returned from REST GET endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorLiveLocationDTO {

    private Long trackingId;
    private Long donorId;
    private Long bloodRequestId;
    private Long hospitalId;

    // ── Donor Identity ─────────────────────────────────────────────────────────
    private String donorName;
    private String donorEmail;
    private String donorPhone;
    private String donorBloodGroup;

    // ── Hospital Context ───────────────────────────────────────────────────────
    private String hospitalName;
    private Double hospitalLatitude;
    private Double hospitalLongitude;

    // ── Live GPS State ─────────────────────────────────────────────────────────
    private Double latitude;
    private Double longitude;
    private Double accuracyMeters;
    private Double speedKmh;
    private Double headingDegrees;
    private Double altitudeMeters;
    private Integer batteryLevel;

    // ── Computed Navigation ────────────────────────────────────────────────────
    /** Remaining distance to hospital in kilometres. */
    private Double distanceRemainingKm;

    /** Remaining distance formatted for display (e.g. "1.2 km" or "450 m"). */
    private String distanceRemainingFormatted;

    /** ETA in minutes. */
    private Integer etaMinutes;

    /** Human-readable ETA (e.g. "12 mins"). */
    private String etaFormatted;

    /** Google Maps navigation deep-link for the donor. */
    private String googleMapsNavigationUrl;

    /** Google Maps embed link showing donor vs hospital on the hospital dashboard map. */
    private String googleMapsEmbedUrl;

    // ── Status ─────────────────────────────────────────────────────────────────
    private TrackingStatus trackingStatus;

    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;
}
