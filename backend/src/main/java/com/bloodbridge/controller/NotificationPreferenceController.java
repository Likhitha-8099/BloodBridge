package com.bloodbridge.controller;

import com.bloodbridge.dto.request.NotificationPreferenceRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.NotificationPreferenceResponse;
import com.bloodbridge.service.NotificationPreferenceService;
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

/**
 * Enterprise REST controller for Notification Preferences and Quiet Hours under /api/v1/preferences.
 */
@RestController
@RequestMapping({"/api/v1/preferences", "/api/v1/notifications/preferences", "/api/preferences"})
@RequiredArgsConstructor
@Tag(name = "Notification Preference Module", description = "Endpoints for configuring channel toggles, quiet hours, and alert preferences")
public class NotificationPreferenceController {

    private static final Logger log = LoggerFactory.getLogger(NotificationPreferenceController.class);

    private final NotificationPreferenceService preferenceService;

    @Operation(summary = "Get Notification Preferences", description = "Retrieves delivery channel, category alert, and quiet hours preferences for authenticated user.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Preferences retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch notification preferences for user: {}", userDetails.getUsername());
        ApiResponse<NotificationPreferenceResponse> response = preferenceService.getUserPreferences(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create Notification Preferences", description = "Initializes notification preferences for authenticated user.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Preferences created successfully")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> createPreferences(
            @RequestBody NotificationPreferenceRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to create notification preferences for user: {}", userDetails.getUsername());
        ApiResponse<NotificationPreferenceResponse> response = preferenceService.updatePreferences(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Notification Preferences", description = "Updates delivery channel, category alert, and quiet hours preferences for authenticated user.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Preferences updated successfully")
    })
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(
            @RequestBody NotificationPreferenceRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to update notification preferences for user: {}", userDetails.getUsername());
        ApiResponse<NotificationPreferenceResponse> response = preferenceService.updatePreferences(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
}
