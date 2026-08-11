package com.bloodbridge.dto.response;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.EligibilityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing the Smart Donor Dashboard summary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Smart Donor Dashboard Response Payload")
public class DonorDashboardResponse {

    @Schema(description = "Profile completion percentage", example = "100")
    private int profileCompletionPercentage;

    @Schema(description = "Blood group", example = "O_POSITIVE")
    private BloodGroup bloodGroup;

    @Schema(description = "Current eligibility status", example = "ELIGIBLE")
    private EligibilityStatus eligibilityStatus;

    @Schema(description = "Days remaining until eligible to donate", example = "0")
    private long daysUntilEligible;

    @Schema(description = "Total completed donations count", example = "5")
    private int totalDonations;

    @Schema(description = "Estimated lives saved", example = "15")
    private int livesSaved;

    @Schema(description = "Nearby active blood requests count", example = "3")
    private int nearbyActiveRequestsCount;

    @Schema(description = "Pending donation requests count", example = "1")
    private int pendingDonationRequests;

    @Schema(description = "Completed donations count", example = "5")
    private int completedDonations;

    @Schema(description = "Recent unread notifications count", example = "2")
    private int recentNotificationsCount;

    @Schema(description = "Smart donor engagement score", example = "120")
    private int donorScore;

    private String fullName;
    private String city;
    private String state;
    private String district;
    private String email;
    private String phoneNumber;
    private String gender;
    private Integer age;
    private Double weight;
    private java.time.LocalDate lastDonationDate;
    private java.time.LocalDate nextEligibleDate;
    private Boolean eligible;
    private Integer cooldownDays;
    private Boolean availableForDonation;
    private Boolean emergencyAvailable;
    private java.time.LocalDateTime createdAt;
    private String healthStatus;
}
