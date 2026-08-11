package com.bloodbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin analytics DTO for the live donor tracking system.
 * Returned by GET /api/v1/admin/analytics/tracking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingAnalyticsDTO {

    /** Number of donors currently in STARTED, MOVING, or STOPPED state. */
    private long activeTrackingSessions;

    /** Average ETA in minutes across all MOVING donors. */
    private Double averageEtaMinutes;

    /** Average speed in km/h across all MOVING donors. */
    private Double averageSpeedKmh;

    /** Percentage of journeys that reached REACHED or COMPLETED status. */
    private Double completionRatePercent;

    /** Total tracking records stored in the system. */
    private long totalTrackingRecords;
}
