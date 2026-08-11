package com.bloodbridge.controller;

import com.bloodbridge.dto.response.*;
import com.bloodbridge.service.HospitalDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Production-Ready REST controller for Hospital Management Dashboard Phase 1 endpoints.
 */
@RestController
@RequestMapping({"/api/hospital/dashboard", "/api/v1/hospital/dashboard", "/api/v1/hospitals/dashboard"})
@RequiredArgsConstructor
@Tag(name = "Hospital Dashboard Module", description = "Endpoints for Hospital Statistics, Emergency Requests, Recent Transfusions, Nearby Donors, and Analytics")
public class HospitalDashboardController {

    private static final Logger log = LoggerFactory.getLogger(HospitalDashboardController.class);

    private final HospitalDashboardService hospitalDashboardService;

    @Operation(summary = "Get Hospital Dashboard Summary", description = "Retrieves composite dashboard summary including statistics metrics, emergency alerts, and recent requests.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard summary retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<HospitalDashboardDTO>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Hospital Dashboard request received for authenticated user: {}", userDetails.getUsername());
        HospitalDashboardDTO data = hospitalDashboardService.getDashboardData(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Hospital dashboard data retrieved successfully", data));
    }

    @Operation(summary = "Get Dashboard Summary Statistics", description = "Retrieves statistics metrics (total requests, pending, accepted, completed, emergency, nearby donors, unread notifications).")
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatisticsDTO>> getStatistics(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Hospital Dashboard statistics request received for: {}", userDetails.getUsername());
        DashboardStatisticsDTO statistics = hospitalDashboardService.getStatistics(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", statistics));
    }

    @Operation(summary = "Get Recent Blood Requests", description = "Retrieves latest blood requests assigned to or managed by the hospital.")
    @GetMapping("/recent-requests")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RecentRequestDTO>>> getRecentRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "5") int limit
    ) {
        log.info("Recent blood requests request received for: {}", userDetails.getUsername());
        List<RecentRequestDTO> requests = hospitalDashboardService.getRecentRequests(userDetails.getUsername(), limit);
        return ResponseEntity.ok(ApiResponse.success("Recent blood requests retrieved successfully", requests));
    }

    @Operation(summary = "Get Emergency Blood Requests", description = "Retrieves emergency and high-priority blood requests for immediate action.")
    @GetMapping("/emergency-requests")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RecentRequestDTO>>> getEmergencyRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "5") int limit
    ) {
        log.info("Emergency blood requests request received for: {}", userDetails.getUsername());
        List<RecentRequestDTO> requests = hospitalDashboardService.getEmergencyRequests(userDetails.getUsername(), limit);
        return ResponseEntity.ok(ApiResponse.success("Emergency blood requests retrieved successfully", requests));
    }

    @Operation(summary = "Get Recent Completed Donations", description = "Retrieves recent completed blood transfusions & donations.")
    @GetMapping("/recent-donations")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RecentDonationDTO>>> getRecentDonations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "5") int limit
    ) {
        log.info("Recent donations request received for: {}", userDetails.getUsername());
        List<RecentDonationDTO> donations = hospitalDashboardService.getRecentDonations(userDetails.getUsername(), limit);
        return ResponseEntity.ok(ApiResponse.success("Recent donations retrieved successfully", donations));
    }

    @Operation(summary = "Get Nearby Available Donors", description = "Retrieves ranked list of nearby available blood donors.")
    @GetMapping("/nearby-donors")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<NearbyDonorDTO>>> getNearbyDonors(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "5") int limit
    ) {
        log.info("Nearby donors request received for: {}", userDetails.getUsername());
        List<NearbyDonorDTO> donors = hospitalDashboardService.getNearbyDonors(userDetails.getUsername(), limit);
        return ResponseEntity.ok(ApiResponse.success("Nearby donors retrieved successfully", donors));
    }

    @Operation(summary = "Get Unread Notifications", description = "Retrieves top unread/recent notifications for the hospital user.")
    @GetMapping("/notifications")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "5") int limit
    ) {
        log.info("Notifications request received for: {}", userDetails.getUsername());
        List<NotificationDTO> notifications = hospitalDashboardService.getNotifications(userDetails.getUsername(), limit);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    @Operation(summary = "Get Dashboard Analytics", description = "Retrieves monthly blood request trends and blood group distribution analytics.")
    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<AnalyticsDTO>> getAnalytics(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Dashboard analytics request received for: {}", userDetails.getUsername());
        AnalyticsDTO analytics = hospitalDashboardService.getAnalytics(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Analytics retrieved successfully", analytics));
    }
}
