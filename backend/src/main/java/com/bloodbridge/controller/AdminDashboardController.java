package com.bloodbridge.controller;

import com.bloodbridge.dto.*;
import com.bloodbridge.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for Admin Dashboard operations and analytics.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    /**
     * Retrieves the consolidated system dashboard metrics.
     *
     * @return the unified dashboard overview response
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboardOverview() {
        DashboardResponse response = dashboardService.getDashboardOverview();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves user-related statistics.
     *
     * @return user statistics response
     */
    @GetMapping("/statistics/users")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics() {
        UserStatisticsResponse response = dashboardService.getUserStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves blood request metrics.
     *
     * @return request statistics response
     */
    @GetMapping("/statistics/requests")
    public ResponseEntity<RequestStatisticsResponse> getRequestStatistics() {
        RequestStatisticsResponse response = dashboardService.getRequestStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves donation transaction metrics.
     *
     * @return donation statistics response
     */
    @GetMapping("/statistics/donations")
    public ResponseEntity<DonationStatisticsResponse> getDonationStatistics() {
        DonationStatisticsResponse response = dashboardService.getDonationStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves matching statistics.
     *
     * @return matching statistics response
     */
    @GetMapping("/statistics/matching")
    public ResponseEntity<MatchingStatisticsResponse> getMatchingStatistics() {
        MatchingStatisticsResponse response = dashboardService.getMatchingStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves notifications metrics.
     *
     * @return notification statistics response
     */
    @GetMapping("/statistics/notifications")
    public ResponseEntity<NotificationStatisticsResponse> getNotificationStatistics() {
        NotificationStatisticsResponse response = dashboardService.getNotificationStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves blood group distribution analytics.
     *
     * @return blood group analytics response
     */
    @GetMapping("/analytics/blood-groups")
    public ResponseEntity<BloodGroupAnalyticsResponse> getBloodGroupAnalytics() {
        BloodGroupAnalyticsResponse response = dashboardService.getBloodGroupAnalytics();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the top 10 donors.
     *
     * @return list of top donors
     */
    @GetMapping("/analytics/top-donors")
    public ResponseEntity<List<TopDonorResponse>> getTopDonors() {
        List<TopDonorResponse> response = dashboardService.getTopDonors();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the top 10 hospitals.
     *
     * @return list of top hospitals
     */
    @GetMapping("/analytics/top-hospitals")
    public ResponseEntity<List<TopHospitalResponse>> getTopHospitals() {
        List<TopHospitalResponse> response = dashboardService.getTopHospitals();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves donation monthly trends over the last 12 months.
     *
     * @return list of monthly trend responses
     */
    @GetMapping("/analytics/monthly-donations")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyDonationTrends() {
        List<MonthlyTrendResponse> response = dashboardService.getMonthlyDonationTrends();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves blood request monthly trends over the last 12 months.
     *
     * @return list of monthly trend responses
     */
    @GetMapping("/analytics/monthly-requests")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyRequestTrends() {
        List<MonthlyTrendResponse> response = dashboardService.getMonthlyRequestTrends();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves unified system health and diagnostics metrics.
     *
     * @return system health response
     */
    @GetMapping("/system-health")
    public ResponseEntity<SystemHealthResponse> getSystemHealth() {
        SystemHealthResponse response = dashboardService.getSystemHealth();
        return ResponseEntity.ok(response);
    }
}
