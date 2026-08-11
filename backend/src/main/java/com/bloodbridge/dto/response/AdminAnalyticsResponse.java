package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Data Transfer Object representing System Analytics and Chart Data API metrics.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Admin Platform Analytics Response Payload")
public class AdminAnalyticsResponse {

    @Schema(description = "Blood group demand breakdown map (e.g. O_POSITIVE -> count)")
    private Map<String, Integer> bloodGroupDemand;

    @Schema(description = "Monthly donation trend map (e.g. 2026-08 -> count)")
    private Map<String, Integer> donationTrends;

    @Schema(description = "Hospital performance metrics map")
    private Map<String, Integer> hospitalPerformance;

    @Schema(description = "Emergency response time statistics in hours")
    private Map<String, Double> emergencyResponseTimes;

    @Schema(description = "Blood inventory consumption units map")
    private Map<String, Integer> inventoryConsumption;
}
