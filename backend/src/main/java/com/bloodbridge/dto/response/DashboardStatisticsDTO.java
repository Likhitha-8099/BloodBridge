package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for Hospital Dashboard summary statistics.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Hospital Dashboard Summary Statistics Payload")
public class DashboardStatisticsDTO {

    @Schema(description = "Total blood requests count", example = "42")
    private long totalRequests;

    @Schema(description = "Pending requests count", example = "8")
    private long pendingRequests;

    @Schema(description = "Accepted or verified requests count", example = "15")
    private long acceptedRequests;

    @Schema(description = "Completed donations count", example = "19")
    private long completedDonations;

    @Schema(description = "Emergency blood requests count", example = "3")
    private long emergencyRequests;

    @Schema(description = "Total nearby available donors count", example = "24")
    private long nearbyDonors;

    @Schema(description = "Unread notifications count", example = "5")
    private long unreadNotifications;
}
