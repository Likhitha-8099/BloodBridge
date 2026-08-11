package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Composite Data Transfer Object combining all Hospital Dashboard sections.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Full Hospital Dashboard Response Payload")
public class HospitalDashboardDTO {

    @Schema(description = "Hospital Name", example = "City General Hospital")
    private String hospitalName;

    @Schema(description = "Hospital Verification Status", example = "APPROVED")
    private String verificationStatus;

    @Schema(description = "Dashboard Statistics Metrics")
    private DashboardStatisticsDTO statistics;

    @Schema(description = "Recent 5 Blood Requests")
    private List<RecentRequestDTO> recentRequests;

    @Schema(description = "Emergency Blood Requests")
    private List<RecentRequestDTO> emergencyRequests;

    @Schema(description = "Recent Completed Donations")
    private List<RecentDonationDTO> recentDonations;

    @Schema(description = "Top Nearby Donors")
    private List<NearbyDonorDTO> nearbyDonors;

    @Schema(description = "Recent Notifications")
    private List<NotificationDTO> notifications;

    @Schema(description = "Dashboard Analytics Data")
    private AnalyticsDTO analytics;
}
