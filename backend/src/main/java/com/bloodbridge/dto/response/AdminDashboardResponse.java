package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing Executive Admin Dashboard KPIs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Admin Dashboard Executive KPIs Response Payload")
public class AdminDashboardResponse {

    @Schema(description = "Total registered users across platform", example = "150")
    private long totalUsers;

    @Schema(description = "Total registered donors", example = "85")
    private long totalDonors;

    @Schema(description = "Total registered patients", example = "45")
    private long totalPatients;

    @Schema(description = "Total registered hospitals", example = "20")
    private long totalHospitals;

    @Schema(description = "Verified hospitals count", example = "16")
    private long verifiedHospitals;

    @Schema(description = "Pending hospital verification approvals count", example = "4")
    private long pendingHospitalApprovals;

    @Schema(description = "Today's created blood requests count", example = "8")
    private long todaysRequests;

    @Schema(description = "Active blood requests count", example = "12")
    private long activeRequests;

    @Schema(description = "Completed blood requests count", example = "65")
    private long completedRequests;

    @Schema(description = "Emergency critical blood requests count", example = "3")
    private long emergencyRequests;

    @Schema(description = "Today's completed blood donations", example = "5")
    private long todaysDonations;

    @Schema(description = "Total completed blood donations", example = "110")
    private long totalDonations;

    @Schema(description = "Estimated total lives saved", example = "330")
    private long livesSaved;

    @Schema(description = "Matching algorithm success rate percentage", example = "94.2")
    private double matchingSuccessRatePct;

    @Schema(description = "Average response time in hours", example = "1.2")
    private double averageResponseTimeHours;

    @Schema(description = "Daily new user registrations count", example = "6")
    private long dailyRegistrations;

    @Schema(description = "Weekly user growth percentage", example = "12.5")
    private double weeklyGrowthPct;

    @Schema(description = "Monthly user growth percentage", example = "35.0")
    private double monthlyGrowthPct;

    @Schema(description = "Total emergency HTML emails dispatched today", example = "12")
    private long emailsSentToday;

    @Schema(description = "Total email delivery failures logged", example = "0")
    private long emailsFailed;

    @Schema(description = "Total emergency blood request emails dispatched across platform", example = "45")
    private long totalEmergencyEmails;
}
