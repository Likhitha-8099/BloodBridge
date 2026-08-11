package com.bloodbridge.service;

import com.bloodbridge.dto.*;
import java.util.List;

/**
 * Service interface for Dashboard data collection and system analytics.
 */
public interface DashboardService {

    /**
     * Retrieves the consolidated dashboard overview stats.
     *
     * @return the unified dashboard response
     */
    DashboardResponse getDashboardOverview();

    /**
     * Collects statistics about users and profiles.
     *
     * @return user statistics response
     */
    UserStatisticsResponse getUserStatistics();

    /**
     * Collects statistics about blood requests.
     *
     * @return request statistics response
     */
    RequestStatisticsResponse getRequestStatistics();

    /**
     * Collects statistics about donation cycles.
     *
     * @return donation statistics response
     */
    DonationStatisticsResponse getDonationStatistics();

    /**
     * Collects statistics about matching runs and success rates.
     *
     * @return matching statistics response
     */
    MatchingStatisticsResponse getMatchingStatistics();

    /**
     * Collects statistics about notification events and delivery channels.
     *
     * @return notification statistics response
     */
    NotificationStatisticsResponse getNotificationStatistics();

    /**
     * Generates distribution analytics of blood groups across donors and requests.
     *
     * @return blood group analytics response
     */
    BloodGroupAnalyticsResponse getBloodGroupAnalytics();

    /**
     * Ranks the top 10 donors.
     *
     * @return a list of top donors
     */
    List<TopDonorResponse> getTopDonors();

    /**
     * Ranks the top 10 hospitals by activity.
     *
     * @return a list of top hospitals
     */
    List<TopHospitalResponse> getTopHospitals();

    /**
     * Computes monthly donation trends for the last 12 months.
     *
     * @return a list of monthly trend responses
     */
    List<MonthlyTrendResponse> getMonthlyDonationTrends();

    /**
     * Computes monthly blood request trends for the last 12 months.
     *
     * @return a list of monthly trend responses
     */
    List<MonthlyTrendResponse> getMonthlyRequestTrends();

    /**
     * Compiles consolidated health and runtime diagnostics metrics.
     *
     * @return system health response
     */
    SystemHealthResponse getSystemHealth();

    /**
     * Collects comprehensive analytics metrics for Push and Multi-Channel Notification delivery.
     *
     * @return push analytics response
     */
    com.bloodbridge.dto.response.PushAnalyticsResponse getPushNotificationAnalytics();
}
