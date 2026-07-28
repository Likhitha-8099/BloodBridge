package com.bloodbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing user statistics for the admin dashboard.
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
}
