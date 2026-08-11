package com.bloodbridge.controller;

import com.bloodbridge.dto.*;
import com.bloodbridge.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Enterprise REST controller for Admin Analytics & Platform Demographics endpoints under /api/v1/admin/analytics and /api/v1/admin/statistics.
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/admin", "/api/admin"})
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Analytics Module", description = "Endpoints for Hospital Leaderboards, Donor Ranks, Demand Charts, and Operational Statistics")
public class AdminAnalyticsController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get Top Hospitals Leaderboard", description = "Retrieves top 10 hospitals ranked by completed blood donations.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Top hospitals retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping({"/analytics/top-hospitals", "/top-hospitals"})
    public ResponseEntity<List<TopHospitalResponse>> getTopHospitals() {
        log.info("Controller request: GET /api/admin/analytics/top-hospitals");
        try {
            List<TopHospitalResponse> response = dashboardService.getTopHospitals();
            log.info("Successfully fetched top hospitals list (count: {})", response != null ? response.size() : 0);
            return ResponseEntity.ok(response != null ? response : java.util.Collections.emptyList());
        } catch (Exception e) {
            log.error("Error processing GET /api/admin/analytics/top-hospitals: {}", e.getMessage(), e);
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    @Operation(summary = "Get Top Donors Leaderboard", description = "Retrieves top 10 donors ranked by donation count.")
    @GetMapping({"/analytics/top-donors", "/top-donors"})
    public ResponseEntity<List<TopDonorResponse>> getTopDonors() {
        log.info("Controller request: GET /api/admin/analytics/top-donors");
        try {
            List<TopDonorResponse> response = dashboardService.getTopDonors();
            return ResponseEntity.ok(response != null ? response : java.util.Collections.emptyList());
        } catch (Exception e) {
            log.error("Error processing GET /api/admin/analytics/top-donors: {}", e.getMessage(), e);
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    @Operation(summary = "Get Blood Group Demographics", description = "Retrieves donor and request distribution across all 8 blood groups.")
    @GetMapping({"/analytics/blood-groups", "/blood-groups"})
    public ResponseEntity<BloodGroupAnalyticsResponse> getBloodGroupAnalytics() {
        log.info("Controller request: GET /api/admin/analytics/blood-groups");
        try {
            BloodGroupAnalyticsResponse response = dashboardService.getBloodGroupAnalytics();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing GET /api/admin/analytics/blood-groups: {}", e.getMessage(), e);
            java.util.Map<String, Long> emptyMap = new java.util.LinkedHashMap<>();
            for (com.bloodbridge.enums.BloodGroup bg : com.bloodbridge.enums.BloodGroup.values()) {
                emptyMap.put(bg.name(), 0L);
            }
            return ResponseEntity.ok(BloodGroupAnalyticsResponse.builder()
                    .donorDistribution(emptyMap)
                    .requestDistribution(new java.util.LinkedHashMap<>(emptyMap))
                    .build());
        }
    }

    @Operation(summary = "Get Monthly Donation Trends", description = "Retrieves 12-month rolling donation counts.")
    @GetMapping({"/analytics/monthly-donations", "/monthly-donations"})
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyDonations() {
        log.info("Controller request: GET /api/admin/analytics/monthly-donations");
        List<MonthlyTrendResponse> response = dashboardService.getMonthlyDonationTrends();
        return ResponseEntity.ok(response != null ? response : List.of());
    }

    @Operation(summary = "Get Monthly Request Trends", description = "Retrieves 12-month rolling request counts.")
    @GetMapping({"/analytics/monthly-requests", "/monthly-requests"})
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyRequests() {
        log.info("Controller request: GET /api/admin/analytics/monthly-requests");
        List<MonthlyTrendResponse> response = dashboardService.getMonthlyRequestTrends();
        return ResponseEntity.ok(response != null ? response : List.of());
    }

    @Operation(summary = "Get User Demographics Statistics", description = "Retrieves breakdown of Donors, Patients, Hospitals, and active/inactive counts.")
    @GetMapping({"/statistics/users", "/analytics/users", "/users/statistics"})
    public ResponseEntity<UserStatisticsResponse> getUserStatistics() {
        log.info("Controller request: GET /api/admin/statistics/users");
        UserStatisticsResponse response = dashboardService.getUserStatistics();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Request Operational Statistics", description = "Retrieves breakdown of requests by status (Pending, Verified, Matched, Completed, Cancelled).")
    @GetMapping({"/statistics/requests", "/analytics/requests", "/requests/statistics"})
    public ResponseEntity<RequestStatisticsResponse> getRequestStatistics() {
        log.info("Controller request: GET /api/admin/statistics/requests");
        RequestStatisticsResponse response = dashboardService.getRequestStatistics();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Donation Operational Statistics", description = "Retrieves breakdown of completed, pending, and cancelled donations.")
    @GetMapping({"/statistics/donations", "/analytics/donations", "/donations/statistics"})
    public ResponseEntity<DonationStatisticsResponse> getDonationStatistics() {
        log.info("Controller request: GET /api/admin/statistics/donations");
        DonationStatisticsResponse response = dashboardService.getDonationStatistics();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Matching Engine Statistics", description = "Retrieves total matches, acceptance rates, and success ratios.")
    @GetMapping({"/statistics/matching", "/analytics/matching", "/matching/statistics"})
    public ResponseEntity<MatchingStatisticsResponse> getMatchingStatistics() {
        log.info("Controller request: GET /api/admin/statistics/matching");
        MatchingStatisticsResponse response = dashboardService.getMatchingStatistics();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Notification Queue Statistics", description = "Retrieves sent, failed, and unread notification metrics.")
    @GetMapping({"/statistics/notifications", "/analytics/notifications", "/notifications/statistics"})
    public ResponseEntity<NotificationStatisticsResponse> getNotificationStatistics() {
        log.info("Controller request: GET /api/admin/statistics/notifications");
        NotificationStatisticsResponse response = dashboardService.getNotificationStatistics();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Push & Multi-Channel Notification Analytics", description = "Retrieves FCM Push, Email, WebSocket, Latency, and Invalid Token metrics.")
    @GetMapping({"/analytics/push-notifications", "/push-notifications"})
    public ResponseEntity<com.bloodbridge.dto.response.PushAnalyticsResponse> getPushNotificationAnalytics() {
        log.info("Controller request: GET /api/admin/analytics/push-notifications");
        com.bloodbridge.dto.response.PushAnalyticsResponse response = dashboardService.getPushNotificationAnalytics();
        return ResponseEntity.ok(response);
    }
}
