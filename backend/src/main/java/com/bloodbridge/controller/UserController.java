package com.bloodbridge.controller;

import com.bloodbridge.dto.request.ChangePasswordRequest;
import com.bloodbridge.dto.request.ProfileImageUploadRequest;
import com.bloodbridge.dto.request.UpdateProfileRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.UserPageResponse;
import com.bloodbridge.dto.response.UserProfileResponse;
import com.bloodbridge.enums.Role;
import com.bloodbridge.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Enterprise REST controller for User Management & Profile Foundation endpoints under /api/v1/users.
 */
@RestController
@RequestMapping({"/api/v1/users", "/api/users"})
@RequiredArgsConstructor
@Tag(name = "User Management Module", description = "Endpoints for Profile Management, Security Credentials, Profile Pictures, and Admin User Governance")
public class UserController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Operation(summary = "View My Profile", description = "Retrieves full profile details for the currently authenticated user.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User profile retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to view profile for user: {}", userDetails.getUsername());
        ApiResponse<UserProfileResponse> response = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update My Profile", description = "Updates common profile information for the authenticated user.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to update profile for user: {}", userDetails.getUsername());
        ApiResponse<UserProfileResponse> response = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Change Password", description = "Changes password after verifying existing password and validating new password strength.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Incorrect password or mismatch"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to change password for user: {}", userDetails.getUsername());
        ApiResponse<String> response = userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload Profile Image", description = "Sets or updates profile picture URL.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile image updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/upload-profile-image")
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadProfileImage(
            @Valid @RequestBody ProfileImageUploadRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to upload profile image for user: {}", userDetails.getUsername());
        ApiResponse<UserProfileResponse> response = userService.uploadProfileImage(userDetails.getUsername(), request.getImageUrl());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deactivate My Account", description = "Deactivates current user's account.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateAccount(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request for self account deactivation by user: {}", userDetails.getUsername());
        ApiResponse<String> response = userService.deactivateAccount(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // ADMIN ENDPOINTS
    // ==========================================

    @Operation(summary = "Admin: List/Search Users", description = "Lists users with pagination, optional role filter, and keyword search.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User list retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<UserPageResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String query
    ) {
        log.info("Admin request to list users. Page: {}, Size: {}", page, size);
        ApiResponse<UserPageResponse> response = userService.getAllUsers(page, size, role, query);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Admin: Get User by ID", description = "Retrieves profile details for any user by User ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User details retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable Long id) {
        log.info("Admin request to fetch user by ID: {}", id);
        ApiResponse<UserProfileResponse> response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Admin: Activate User Account", description = "Reactivates a deactivated user account.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account activated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<String>> activateUser(@PathVariable Long id) {
        log.info("Admin request to activate user ID: {}", id);
        ApiResponse<String> response = userService.activateUser(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Admin: Deactivate User Account", description = "Deactivates a user account.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateUser(@PathVariable Long id) {
        log.info("Admin request to deactivate user ID: {}", id);
        ApiResponse<String> response = userService.deactivateUser(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Admin: Delete User Account", description = "Permanently deletes a user and cleanly removes associated profile records.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        log.info("Admin request to delete user ID: {}", id);
        ApiResponse<String> response = userService.deleteUser(id);
        return ResponseEntity.ok(response);
    }
}
