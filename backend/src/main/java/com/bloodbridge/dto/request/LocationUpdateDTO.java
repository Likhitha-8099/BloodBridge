package com.bloodbridge.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Inbound payload from the donor device carrying one GPS telemetry snapshot.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateDTO {

    /** The blood request ID this journey relates to. */
    @NotNull(message = "Blood request ID is required")
    private Long bloodRequestId;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0",  message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0",   message = "Latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0",  message = "Longitude must be <= 180")
    private Double longitude;

    /** Horizontal accuracy in metres (device-reported). */
    private Double accuracyMeters;

    /** Device speed in km/h — null when unavailable. */
    private Double speedKmh;

    /** Compass bearing 0-360 degrees — null when unavailable. */
    private Double headingDegrees;

    /** Altitude in metres above sea level — optional. */
    private Double altitudeMeters;

    /** Device battery percentage 0-100 — optional. */
    private Integer batteryLevel;

    /** Device-side UTC timestamp of the GPS fix — defaults to server time if null. */
    private LocalDateTime timestamp;
}
