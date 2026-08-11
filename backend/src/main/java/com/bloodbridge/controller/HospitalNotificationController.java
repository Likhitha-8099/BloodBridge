package com.bloodbridge.controller;

import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.NotificationResponse;
import com.bloodbridge.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
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
 * REST controller for Hospital Notifications and Donor Acceptance alerts.
 * Endpoint mappings support both legacy (/api/hospital/...) and versioned (/api/v1/hospital/...) paths.
 */
@RestController
@RequestMapping({"/api/v1/hospital", "/api/hospital"})
@RequiredArgsConstructor
@Tag(name = "Hospital Notifications Module", description = "Endpoints for hospital notification feeds, donor acceptance alerts, and read status updates")
public class HospitalNotificationController {

    private static final Logger log = LoggerFactory.getLogger(HospitalNotificationController.class);

    private final NotificationService notificationService;

    @Operation(summary = "Get Hospital Notifications", description = "Retrieves recent notifications and donor acceptances for authenticated hospital.")
    @GetMapping("/notifications")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getHospitalNotifications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to fetch hospital notifications for user: {}", userDetails.getUsername());
        ApiResponse<List<NotificationResponse>> response = notificationService.getHospitalNotifications(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark Notification as Read", description = "Marks a specific hospital notification as read by ID.")
    @PutMapping("/notifications/{id}/read")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request by hospital user {} to mark notification #{} as read", userDetails.getUsername(), id);
        ApiResponse<NotificationResponse> response = notificationService.markAsRead(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }
}
