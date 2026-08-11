package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Data Transfer Object representing Hospital Analytics and Demand metrics.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Hospital Analytics Response Payload")
public class HospitalAnalyticsResponse {

    @Schema(description = "Total emergency requests created", example = "25")
    private long totalRequests;

    @Schema(description = "Fulfilled requests count", example = "20")
    private long fulfilledRequests;

    @Schema(description = "Cancelled requests count", example = "2")
    private long cancelledRequests;

    @Schema(description = "Average response time in hours", example = "1.5")
    private double averageResponseTimeHours;

    @Schema(description = "Donation success rate percentage", example = "92.5")
    private double donationSuccessRatePct;

    @Schema(description = "Blood group demand breakdown map")
    private Map<String, Integer> bloodGroupDemand;

    @Schema(description = "Inventory consumption units map")
    private Map<String, Integer> inventoryConsumption;
}
