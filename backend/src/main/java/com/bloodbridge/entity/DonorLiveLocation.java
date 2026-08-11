package com.bloodbridge.entity;

import com.bloodbridge.enums.TrackingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity that stores a donor's real-time GPS telemetry point during an active emergency journey.
 * Each row represents one position snapshot. The most-recent row per (donorId, bloodRequestId)
 * is the authoritative live position used for the hospital tracking map.
 */
@Entity
@Table(
    name = "donor_live_locations",
    indexes = {
        @Index(name = "idx_dll_donor_request",  columnList = "donor_id, blood_request_id"),
        @Index(name = "idx_dll_hospital_id",    columnList = "hospital_id"),
        @Index(name = "idx_dll_status",         columnList = "tracking_status"),
        @Index(name = "idx_dll_last_updated",   columnList = "last_updated")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorLiveLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to the DonorProfile who submitted this GPS update. */
    @Column(name = "donor_id", nullable = false)
    private Long donorId;

    /** FK to the BloodRequest this journey is responding to. */
    @Column(name = "blood_request_id", nullable = false)
    private Long bloodRequestId;

    /** FK to the destination Hospital. */
    @Column(name = "hospital_id", nullable = false)
    private Long hospitalId;

    // ── GPS Payload ────────────────────────────────────────────────────────────

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    /** Horizontal accuracy in metres as reported by the device GPS. */
    @Column(name = "accuracy_meters")
    private Double accuracyMeters;

    /** Speed in km/h — null when the device cannot determine speed. */
    @Column(name = "speed_kmh")
    private Double speedKmh;

    /** Compass bearing 0-360 degrees — null when stationary or unknown. */
    @Column(name = "heading_degrees")
    private Double headingDegrees;

    /** Altitude in metres above sea level — optional. */
    @Column(name = "altitude_meters")
    private Double altitudeMeters;

    /** Device battery percentage 0-100 — optional, used for alerting on low battery. */
    @Column(name = "battery_level")
    private Integer batteryLevel;

    // ── Computed Fields ────────────────────────────────────────────────────────

    /** Haversine or Google-Maps-computed remaining distance in kilometres. */
    @Column(name = "distance_remaining_km")
    private Double distanceRemainingKm;

    /** Estimated time of arrival in minutes. */
    @Column(name = "eta_minutes")
    private Integer etaMinutes;

    // ── State ──────────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_status", nullable = false)
    @Builder.Default
    private TrackingStatus trackingStatus = TrackingStatus.STARTED;

    /** Device-side timestamp of when the GPS fix was captured. */
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // ── Audit ──────────────────────────────────────────────────────────────────

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
