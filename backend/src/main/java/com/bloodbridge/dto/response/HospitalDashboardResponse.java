package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object representing Hospital Dashboard summary metrics.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Hospital Dashboard Summary Response Payload")
public class HospitalDashboardResponse {

    @Schema(description = "Verification status", example = "APPROVED")
    private String verificationStatus;

    @Schema(description = "List of blood inventory items", example = "[]")
    private List<BloodInventoryResponse> inventorySummary;

    @Schema(description = "Today's created blood requests count", example = "4")
    private int todaysRequests;

    @Schema(description = "Emergency blood requests count", example = "2")
    private int emergencyRequestsCount;

    @Schema(description = "Matched compatible donors count", example = "8")
    private int matchedDonorsCount;

    @Schema(description = "Pending donations count", example = "3")
    private int pendingDonationsCount;

    @Schema(description = "Completed donations count", example = "12")
    private int completedDonationsCount;

    @Schema(description = "Low inventory alerts count", example = "1")
    private int lowInventoryAlerts;

    @Schema(description = "Critical inventory alerts count", example = "0")
    private int criticalInventoryAlerts;
}
