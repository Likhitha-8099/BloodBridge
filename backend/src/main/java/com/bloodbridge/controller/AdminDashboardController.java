package com.bloodbridge.controller;

import com.bloodbridge.dto.request.BroadcastAnnouncementRequest;
import com.bloodbridge.dto.response.AdminAnalyticsResponse;
import com.bloodbridge.dto.response.AdminDashboardResponse;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.AuditLogResponse;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.response.DonationHistoryResponse;
import com.bloodbridge.dto.response.GlobalSearchResponse;
import com.bloodbridge.dto.response.HospitalResponse;
import com.bloodbridge.dto.response.SystemHealthResponse;
import com.bloodbridge.dto.response.UserPageResponse;
import com.bloodbridge.dto.response.UserProfileResponse;
import com.bloodbridge.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Enterprise REST controller for Enterprise Admin Dashboard & System Operations Center endpoints under /api/v1/admin.
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/admin", "/api/admin"})
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Operations Center Module", description = "Endpoints for Executive KPIs, Hospital Verification, User Management, Audit Logs, System Health, and Global Search")
public class AdminDashboardController {

    private final AdminService adminService;

    @Operation(summary = "Get Executive Admin Dashboard", description = "Retrieves executive dashboard KPIs summary.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Admin dashboard summary retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboardOverview() {
        log.info("Request to fetch Executive Admin Dashboard overview");
        ApiResponse<AdminDashboardResponse> response = adminService.getDashboard();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Platform Analytics & Chart Data", description = "Retrieves demand maps, donation trends, hospital performance metrics, and inventory consumption.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Analytics retrieved")
    })
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<AdminAnalyticsResponse>> getAnalytics() {
        log.info("Request to fetch platform analytics");
        ApiResponse<AdminAnalyticsResponse> response = adminService.getAnalytics();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Pending Hospitals", description = "Retrieves list of pending hospital registrations for verification review.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pending hospitals retrieved")
    })
    @GetMapping("/hospitals/pending")
    public ResponseEntity<ApiResponse<List<HospitalResponse>>> getPendingHospitals() {
        log.info("Request to fetch pending hospital registrations");
        ApiResponse<List<HospitalResponse>> response = adminService.getPendingHospitals();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verify or Reject Hospital", description = "Reviews and approves, rejects, or suspends a hospital registration.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hospital verification status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hospital not found")
    })
    @org.springframework.web.bind.annotation.RequestMapping(value = "/hospitals/{id}/verify", method = {org.springframework.web.bind.annotation.RequestMethod.PATCH, org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.PUT})
    public ResponseEntity<ApiResponse<HospitalResponse>> verifyHospital(
            @PathVariable Long id,
            @Parameter(description = "Target status (APPROVED, REJECTED, SUSPENDED)") @RequestParam String status,
            @Parameter(description = "Review remarks") @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String adminEmail = (userDetails != null && userDetails.getUsername() != null) ? userDetails.getUsername() : "admin@bloodbridge.com";
        log.info("Admin {} request to verify hospital ID: {} to status: {}", adminEmail, id, status);
        ApiResponse<HospitalResponse> response = adminService.verifyHospital(id, status, remarks, adminEmail);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get All Registered Users", description = "Retrieves paginated list of all users with optional search filtering.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users page retrieved")
    })
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<UserPageResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query
    ) {
        log.info("Request to fetch users list (page: {}, size: {}, query: {})", page, size, query);
        try {
            ApiResponse<UserPageResponse> response = adminService.getAllUsers(page, size, query);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing GET /api/admin/users: {}", e.getMessage(), e);
            UserPageResponse emptyPage = UserPageResponse.builder()
                    .content(java.util.Collections.emptyList())
                    .pageNumber(page)
                    .pageSize(size)
                    .totalElements(0L)
                    .totalPages(0)
                    .last(true)
                    .build();
            return ResponseEntity.ok(ApiResponse.success("Users list retrieved", emptyPage));
        }
    }

    @Operation(summary = "Update User Account Status", description = "Activates, deactivates, or suspends a user account.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User account status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserStatus(
            @PathVariable Long id,
            @Parameter(description = "Target status (ACTIVE, DEACTIVATED, SUSPENDED)") @RequestParam String status,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Admin {} request to update user ID: {} status to: {}", userDetails.getUsername(), id, status);
        ApiResponse<UserProfileResponse> response = adminService.updateUserStatus(id, status, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Monitor All Blood Requests", description = "Retrieves all blood requests across the system for administrative monitoring.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All blood requests retrieved")
    })
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<BloodRequestResponse>>> getAllBloodRequests() {
        log.info("Request to fetch all blood requests for admin monitoring");
        ApiResponse<List<BloodRequestResponse>> response = adminService.getAllBloodRequests();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Force Close Blood Request", description = "Force closes or cancels an active blood request.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blood request force closed")
    })
    @PatchMapping("/requests/{id}/force-close")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> forceCloseBloodRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Admin {} request to force close blood request ID: {}", userDetails.getUsername(), id);
        ApiResponse<BloodRequestResponse> response = adminService.forceCloseBloodRequest(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Monitor All Donations", description = "Retrieves all completed and scheduled donation transactions.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All donation records retrieved")
    })
    @GetMapping("/donations")
    public ResponseEntity<ApiResponse<List<DonationHistoryResponse>>> getAllDonations() {
        log.info("Request to fetch all donation transactions");
        ApiResponse<List<DonationHistoryResponse>> response = adminService.getAllDonations();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get System Audit Logs", description = "Retrieves paginated security audit logs for operations tracking.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit logs retrieved")
    })
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Request to fetch system audit logs");
        ApiResponse<List<AuditLogResponse>> response = adminService.getAuditLogs(page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get System Health & Diagnostics", description = "Retrieves application status, database connectivity, memory usage, and uptime.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "System health metrics retrieved")
    })
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<SystemHealthResponse>> getSystemHealth() {
        log.info("Request to fetch system health & diagnostics");
        ApiResponse<SystemHealthResponse> response = adminService.getSystemHealth();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Global Admin Search", description = "Performs global search keyword query across Users, Hospitals, Requests, and Donations.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Global search executed")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<GlobalSearchResponse>> globalSearch(@RequestParam String query) {
        log.info("Request for global search query: {}", query);
        ApiResponse<GlobalSearchResponse> response = adminService.globalSearch(query);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Broadcast Target Notification", description = "Broadcasts target notification to specific roles, cities, or all users.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Target broadcast sent")
    })
    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<String>> broadcastTargetNotification(
            @Valid @RequestBody BroadcastAnnouncementRequest request,
            @Parameter(description = "Target role filter (optional)") @RequestParam(required = false) String role,
            @Parameter(description = "Target city filter (optional)") @RequestParam(required = false) String targetCity,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Admin {} broadcasting target notification", userDetails.getUsername());
        ApiResponse<String> response = adminService.broadcastTargetNotification(
                userDetails.getUsername(),
                request.getTitle(),
                request.getMessage(),
                role,
                targetCity,
                request.getPriority() != null ? request.getPriority() : "NORMAL"
        );
        return ResponseEntity.ok(response);
    }
}
