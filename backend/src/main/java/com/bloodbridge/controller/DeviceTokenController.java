package com.bloodbridge.controller;

import com.bloodbridge.dto.request.RegisterDeviceTokenRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.DeviceTokenResponse;
import com.bloodbridge.service.DeviceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * Controller for FCM Device Token registration and device management.
 * Phase 3B.1 — Device Registration module.
 */
@RestController
@RequestMapping({"/api/v1/device", "/api/device"})
@RequiredArgsConstructor
@Tag(name = "Device Registration Module", description = "Endpoints for Firebase Cloud Messaging device registration & token lifecycle management")
public class DeviceTokenController {

    private static final Logger log = LoggerFactory.getLogger(DeviceTokenController.class);

    private final DeviceTokenService deviceTokenService;

    @Operation(summary = "Register or Update Device Token", description = "Registers an FCM device token for the authenticated user (upsert pattern).")
    @PostMapping("/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DeviceTokenResponse>> registerDeviceToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RegisterDeviceTokenRequest request) {
        log.info("REST request to register FCM device token for user: {}", userDetails.getUsername());
        ApiResponse<DeviceTokenResponse> response = deviceTokenService.registerToken(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh FCM Device Token", description = "Updates an existing FCM token when rotated by Firebase SDK.")
    @PostMapping("/refresh")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DeviceTokenResponse>> refreshDeviceToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String oldToken,
            @RequestParam String newToken) {
        log.info("REST request to refresh FCM device token for user: {}", userDetails.getUsername());
        ApiResponse<DeviceTokenResponse> response = deviceTokenService.refreshToken(userDetails.getUsername(), oldToken, newToken);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove FCM Device Token", description = "Removes or deactivates an FCM device token upon logout or permission revocation.")
    @DeleteMapping("/remove")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> removeDeviceToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String token) {
        log.info("REST request to remove FCM device token for user: {}", userDetails.getUsername());
        ApiResponse<Void> response = deviceTokenService.removeToken(userDetails.getUsername(), token);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get My Registered Devices", description = "Retrieves all active registered device tokens for the authenticated user.")
    @GetMapping("/my-devices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DeviceTokenResponse>>> getMyRegisteredDevices(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("REST request to fetch registered devices for user: {}", userDetails.getUsername());
        ApiResponse<List<DeviceTokenResponse>> response = deviceTokenService.getUserActiveTokens(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
