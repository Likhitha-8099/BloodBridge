package com.bloodbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Unified response DTO containing all stats sections for the admin dashboard.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private UserStatisticsResponse userStatistics;
    private RequestStatisticsResponse requestStatistics;
    private DonationStatisticsResponse donationStatistics;
    private MatchingStatisticsResponse matchingStatistics;
    private NotificationStatisticsResponse notificationStatistics;
}
