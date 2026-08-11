package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object for Hospital Dashboard Analytics & Chart Visualizations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Hospital Dashboard Analytics Payload")
public class AnalyticsDTO {

    @Schema(description = "Monthly blood requests trend data points")
    private List<MonthlyDataPoint> monthlyRequests;

    @Schema(description = "Blood group distribution data points")
    private List<BloodGroupDataPoint> bloodGroupDistribution;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyDataPoint {
        private String month;
        private long count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BloodGroupDataPoint {
        private String bloodGroup;
        private long count;
    }
}
