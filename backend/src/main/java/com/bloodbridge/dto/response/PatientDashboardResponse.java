package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object representing Patient Dashboard summary metrics.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Patient Dashboard Summary Response Payload")
public class PatientDashboardResponse {

    @Schema(description = "Profile completion percentage", example = "90")
    private int profileCompletionPercentage;

    @Schema(description = "Current active blood requests count", example = "1")
    private int currentBloodRequestsCount;

    @Schema(description = "Completed blood requests count", example = "3")
    private int completedRequestsCount;

    @Schema(description = "Pending verification requests count", example = "1")
    private int pendingRequestsCount;

    @Schema(description = "Matched compatible donors count across active requests", example = "4")
    private int matchedDonorsCount;

    @Schema(description = "Selected hospital name", example = "Boston General Hospital")
    private String hospitalName;

    @Schema(description = "Emergency contact name", example = "David Smith")
    private String emergencyContactName;

    @Schema(description = "Emergency contact phone number", example = "+16175550188")
    private String emergencyContactNumber;

    @Schema(description = "Recent notification count", example = "2")
    private int recentNotificationsCount;

    @Schema(description = "Medical history summary", example = "Severe Anemia; No drug allergies")
    private String medicalSummary;

    @Schema(description = "Recent blood request list")
    private List<BloodRequestResponse> recentRequests;
}
