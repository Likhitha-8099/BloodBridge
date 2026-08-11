package com.bloodbridge.controller;

import com.bloodbridge.dto.request.CreateBloodRequestRequest;
import com.bloodbridge.dto.request.CreatePatientProfileRequest;
import com.bloodbridge.dto.request.UpdateBloodRequestRequest;
import com.bloodbridge.dto.request.UpdatePatientProfileRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.response.PatientDashboardResponse;
import com.bloodbridge.dto.response.PatientProfileResponse;
import com.bloodbridge.dto.response.RequestTimelineResponse;
import com.bloodbridge.service.PatientProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Enterprise REST controller for Patient Management & Emergency Blood Request Portal endpoints under /api/v1/patients.
 */
@RestController
@RequestMapping({"/api/v1/patients", "/api/patients"})
@RequiredArgsConstructor
@Tag(name = "Patient Management Module", description = "Endpoints for Patient Profile, Emergency Blood Requests, Request Status Tracking, and Patient Dashboard")
public class PatientController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PatientController.class);

    private final PatientProfileService patientProfileService;

    @Operation(summary = "Create Patient Profile", description = "Creates a new patient medical profile for the authenticated user with role PATIENT.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Patient profile created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or profile already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> createProfile(
            @Valid @RequestBody CreatePatientProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to create patient profile for user: {}", userDetails.getUsername());
        ApiResponse<PatientProfileResponse> response = patientProfileService.createProfile(userDetails.getUsername(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "View My Patient Profile", description = "Retrieves patient medical profile details.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient profile retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient profile not found")
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch patient profile for user: {}", userDetails.getUsername());
        ApiResponse<PatientProfileResponse> response = patientProfileService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update My Patient Profile", description = "Updates patient medical details, emergency contact, and location.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient profile updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> updateProfile(
            @Valid @RequestBody UpdatePatientProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to update patient profile for user: {}", userDetails.getUsername());
        ApiResponse<PatientProfileResponse> response = patientProfileService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deactivate My Patient Profile", description = "Soft deletes and deactivates patient profile.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient profile deactivated")
    })
    @DeleteMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<String>> deleteProfile(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to soft delete patient profile for user: {}", userDetails.getUsername());
        ApiResponse<String> response = patientProfileService.deleteProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Patient Dashboard", description = "Returns patient dashboard summary, profile completion %, active requests, and emergency contacts.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard summary retrieved")
    })
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PatientDashboardResponse>> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch patient dashboard for user: {}", userDetails.getUsername());
        ApiResponse<PatientDashboardResponse> response = patientProfileService.getDashboard(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create Emergency Blood Request", description = "Creates a new emergency blood request ticket for the patient.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Emergency blood request created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/blood-requests")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> createBloodRequest(
            @Valid @RequestBody CreateBloodRequestRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to create emergency blood request by patient user: {}", userDetails.getUsername());
        ApiResponse<BloodRequestResponse> response = patientProfileService.createBloodRequest(userDetails.getUsername(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get My Blood Requests", description = "Retrieves all blood requests created by the patient.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient blood requests retrieved")
    })
    @GetMapping("/blood-requests")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<BloodRequestResponse>>> getMyBloodRequests(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch blood requests for user: {}", userDetails.getUsername());
        ApiResponse<List<BloodRequestResponse>> response = patientProfileService.getMyBloodRequests(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Blood Request Details", description = "Retrieves detailed information of a specific blood request.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blood request details retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blood request not found")
    })
    @GetMapping("/blood-requests/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> getBloodRequestById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to fetch blood request ID: {} for user: {}", id, userDetails.getUsername());
        ApiResponse<BloodRequestResponse> response = patientProfileService.getBloodRequestById(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Blood Request", description = "Updates an uncompleted emergency blood request.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blood request updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or invalid state")
    })
    @PutMapping("/blood-requests/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> updateBloodRequest(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBloodRequestRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to update blood request ID: {} for user: {}", id, userDetails.getUsername());
        ApiResponse<BloodRequestResponse> response = patientProfileService.updateBloodRequest(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel Blood Request", description = "Cancels an active blood request.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blood request cancelled")
    })
    @DeleteMapping("/blood-requests/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> cancelBloodRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to cancel blood request ID: {} for user: {}", id, userDetails.getUsername());
        ApiResponse<BloodRequestResponse> response = patientProfileService.cancelBloodRequest(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Blood Request Real-Time Timeline", description = "Retrieves real-time chronological progress timeline for an emergency blood request.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request timeline retrieved")
    })
    @GetMapping("/blood-requests/{id}/timeline")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<RequestTimelineResponse>> getBloodRequestTimeline(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to fetch blood request timeline for ID: {} for user: {}", id, userDetails.getUsername());
        ApiResponse<RequestTimelineResponse> response = patientProfileService.getBloodRequestTimeline(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }
}
