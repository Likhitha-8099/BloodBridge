package com.bloodbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Data Transfer Object containing analytics, counts, and trends for donations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationStatisticsResponse {

    private Long totalDonations;
    private Long completedDonations;
    private Long pendingDonations;
    private Long cancelledDonations;
    private Map<String, Long> donationsByBloodGroup;
    private Map<String, Long> topDonors;
    private Map<String, Long> mostActiveHospitals;
    private Map<String, Long> monthlyDonationTrends;
    private Double donationCompletionRate;
}
