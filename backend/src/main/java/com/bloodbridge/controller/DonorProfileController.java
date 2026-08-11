package com.bloodbridge.controller;

import com.bloodbridge.dto.request.AvailabilityRequest;
import com.bloodbridge.dto.request.CreateDonorProfileRequest;
import com.bloodbridge.dto.request.EmergencyAvailabilityRequest;
import com.bloodbridge.dto.request.PreferredRadiusRequest;
import com.bloodbridge.dto.request.UpdateDonorProfileRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.DonationHistoryResponse;
import com.bloodbridge.dto.response.DonorDashboardResponse;
import com.bloodbridge.dto.response.DonorProfileResponse;
import com.bloodbridge.dto.response.EligibilityResponse;
import com.bloodbridge.service.DonorProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Enterprise REST controller for Donor Management & Smart Donor Portal endpoints under /api/v1/donors.
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/donors", "/api/donors"})
@PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Donor Management Module", description = "Endpoints for Donor Medical Profiles, Smart Eligibility Engine, Donor Scoring, Dashboard & History")
public class DonorProfileController {

    private final DonorProfileService donorProfileService;
    private final com.bloodbridge.service.DonationService donationService;
    private final com.bloodbridge.service.CertificateService certificateService;

    @Operation(summary = "Create Donor Profile", description = "Creates a new donor medical profile for the authenticated donor.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Donor profile created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or profile already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<DonorProfileResponse>> createProfile(
            @Valid @RequestBody CreateDonorProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to create donor profile for user: {}", userDetails.getUsername());
        ApiResponse<DonorProfileResponse> response = donorProfileService.createProfile(userDetails.getUsername(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "View My Donor Profile", description = "Retrieves complete donor medical profile details.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Donor profile retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Donor profile not found")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch donor profile for user: {}", userDetails.getUsername());
        ApiResponse<DonorProfileResponse> response = donorProfileService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update My Donor Profile", description = "Updates donor medical parameters, location, and contact preferences.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Donor profile updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateDonorProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to update donor profile for user: {}", userDetails.getUsername());
        ApiResponse<DonorProfileResponse> response = donorProfileService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Toggle Donation Availability", description = "Toggles regular availability for blood donation.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Availability updated")
    })
    @PatchMapping("/availability")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> toggleAvailability(
            @Valid @RequestBody AvailabilityRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to toggle availability for user: {}", userDetails.getUsername());
        ApiResponse<DonorProfileResponse> response = donorProfileService.toggleAvailability(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Emergency Availability", description = "Toggles availability for urgent emergency blood calls.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Emergency availability updated")
    })
    @PatchMapping("/emergency-availability")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateEmergencyAvailability(
            @Valid @RequestBody EmergencyAvailabilityRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to update emergency availability for user: {}", userDetails.getUsername());
        ApiResponse<DonorProfileResponse> response = donorProfileService.updateEmergencyAvailability(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Preferred Donation Radius", description = "Updates preferred maximum travel distance radius (KM).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Preferred radius updated")
    })
    @PatchMapping("/preferred-radius")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updatePreferredRadius(
            @Valid @RequestBody PreferredRadiusRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to update preferred radius for user: {}", userDetails.getUsername());
        ApiResponse<DonorProfileResponse> response = donorProfileService.updatePreferredRadius(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Smart Donor Dashboard", description = "Returns dashboard metrics including completion %, donor score, lives saved, and eligibility countdown.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard summary retrieved")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DonorDashboardResponse>> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch donor dashboard summary for user: {}", userDetails.getUsername());
        ApiResponse<DonorDashboardResponse> response = donorProfileService.getDashboard(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Smart Eligibility Details", description = "Runs smart eligibility engine to calculate donation readiness, next eligible date, and health recommendations.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Eligibility details retrieved")
    })
    @GetMapping("/eligibility")
    public ResponseEntity<ApiResponse<EligibilityResponse>> calculateEligibility(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to compute eligibility for user: {}", userDetails.getUsername());
        ApiResponse<EligibilityResponse> response = donorProfileService.calculateEligibility(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Donation History Timeline", description = "Returns complete timeline of past donations, certificates, and medical notes.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Donation history timeline retrieved")
    })
    @GetMapping("/donation-history")
    public ResponseEntity<ApiResponse<List<DonationHistoryResponse>>> getDonationHistory(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch donation history for user: {}", userDetails.getUsername());
        ApiResponse<List<DonationHistoryResponse>> response = donorProfileService.getDonationHistory(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Authenticated Donor Donations", description = "Returns complete list of past donations with certificate readiness flags.")
    @GetMapping("/donations")
    public ResponseEntity<ApiResponse<List<com.bloodbridge.dto.DonationSummaryResponse>>> getMyDonations(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch donor donations for user: {}", userDetails.getUsername());
        List<com.bloodbridge.dto.DonationSummaryResponse> history = donationService.getMyDonations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Donations retrieved successfully", history));
    }

    @Operation(summary = "Download Blood Donation Certificate PDF", description = "Downloads PDF certificate for a completed blood donation.")
    @GetMapping("/donations/{donationId}/certificate")
    public ResponseEntity<byte[]> getMyDonationCertificate(
            @PathVariable Long donationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to download donation certificate #{} for user: {}", donationId, userDetails.getUsername());
        byte[] pdf = certificateService.getCertificatePdfForDonor(donationId, userDetails.getUsername());
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"BloodBridge_Certificate_" + donationId + ".pdf\"")
                .body(pdf);
    }

    @Operation(summary = "Deactivate My Donor Profile", description = "Soft deletes and deactivates donor profile.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Donor profile deactivated")
    })
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteProfile(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to soft delete donor profile for user: {}", userDetails.getUsername());
        ApiResponse<String> response = donorProfileService.deleteProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
