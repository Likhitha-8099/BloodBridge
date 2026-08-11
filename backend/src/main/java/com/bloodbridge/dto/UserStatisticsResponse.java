package com.bloodbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * DTO representing user demographics and analytics for the admin dashboard.
 * Extends basic user counts with rich real-time database demographic metrics.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatisticsResponse {
    private Long totalUsers;
    private Long totalDonors;
    private Long totalPatients;
    private Long totalHospitals;
    private Long activeUsers;
    private Long inactiveUsers;

    // Real Demographics & Analytics Metrics
    private Long availableDonors;
    private Long emergencyAvailableDonors;
    private Long eligibleDonors;
    private Long cooldownDonors;
    
    private Map<String, Long> bloodGroupDistribution;
    private Map<String, Long> genderDistribution;
    private Map<String, Long> ageGroupDistribution;
    private Map<String, Long> roleDistribution;
    private Map<String, Long> locationCityDistribution;
    private Map<String, Long> locationStateDistribution;
    private Map<String, Long> availabilityDistribution;
    
    private List<Map<String, Object>> monthlyRegistrationTrends;
    private List<String> automatedInsights;
}
