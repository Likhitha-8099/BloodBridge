package com.bloodbridge.controller;

import com.bloodbridge.dto.request.CreateHospitalRequest;
import com.bloodbridge.dto.request.DocumentUploadRequest;
import com.bloodbridge.dto.request.HospitalBloodRequestCreate;
import com.bloodbridge.dto.request.UpdateHospitalRequest;
import com.bloodbridge.dto.request.UpdateInventoryRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.BloodInventoryResponse;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.response.DonorMatchViewResponse;
import com.bloodbridge.dto.response.HospitalAnalyticsResponse;
import com.bloodbridge.dto.response.HospitalDashboardResponse;
import com.bloodbridge.dto.response.HospitalResponse;
import com.bloodbridge.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Enterprise REST controller for Hospital Management, Blood Inventory & Emergency Request Center endpoints under /api/v1/hospitals.
 */
@RestController
@RequestMapping({"/api/v1/hospitals", "/api/hospitals", "/api/v1/hospital", "/api/hospital"})
@RequiredArgsConstructor
@Tag(name = "Hospital Management Module", description = "Endpoints for Hospital Profile, License Upload, Blood Inventory Stock, Emergency Requests, Matched Donors, and Analytics")
public class HospitalController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HospitalController.class);

    private final HospitalService hospitalService;

    @Operation(summary = "Create Hospital Profile", description = "Creates a new hospital profile for the authenticated user with role HOSPITAL.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Hospital profile created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or profile already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @PreAuthorize("hasRole('HOSPITAL')")
    public ResponseEntity<ApiResponse<HospitalResponse>> createHospital(
            @Valid @RequestBody CreateHospitalRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to create hospital profile for user: {}", userDetails.getUsername());
        ApiResponse<HospitalResponse> response = hospitalService.createHospital(userDetails.getUsername(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "View My Hospital Profile", description = "Retrieves hospital profile details and verification status.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hospital profile retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hospital profile not found")
    })
    @GetMapping({"/me", "/profile"})
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<HospitalResponse>> getMyHospital(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch hospital profile for user: {}", userDetails.getUsername());
        ApiResponse<HospitalResponse> response = hospitalService.getMyHospital(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update My Hospital Profile", description = "Updates hospital contact details, operating hours, and location.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hospital profile updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PutMapping({"/me", "/profile"})
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<HospitalResponse>> updateHospital(
            @Valid @RequestBody UpdateHospitalRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to update hospital profile for user: {}", userDetails.getUsername());
        ApiResponse<HospitalResponse> response = hospitalService.updateHospital(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload License Document", description = "Uploads or sets medical license document URL for admin verification.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "License document uploaded")
    })
    @PostMapping("/upload-license")
    @PreAuthorize("hasRole('HOSPITAL')")
    public ResponseEntity<ApiResponse<HospitalResponse>> uploadLicense(
            @Valid @RequestBody DocumentUploadRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to upload license document for user: {}", userDetails.getUsername());
        ApiResponse<HospitalResponse> response = hospitalService.uploadLicense(userDetails.getUsername(), request.getDocumentUrl());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload Hospital Logo", description = "Uploads or sets hospital branding logo URL.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hospital logo uploaded")
    })
    @PostMapping("/upload-logo")
    @PreAuthorize("hasRole('HOSPITAL')")
    public ResponseEntity<ApiResponse<HospitalResponse>> uploadLogo(
            @Valid @RequestBody DocumentUploadRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to upload hospital logo for user: {}", userDetails.getUsername());
        ApiResponse<HospitalResponse> response = hospitalService.uploadLogo(userDetails.getUsername(), request.getDocumentUrl());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Hospital Dashboard Overview", description = "Returns hospital dashboard summary metrics, inventory alerts, and request counts.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard summary retrieved")
    })
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<HospitalDashboardResponse>> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch hospital dashboard overview for user: {}", userDetails.getUsername());
        ApiResponse<HospitalDashboardResponse> response = hospitalService.getDashboard(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Hospital Profile Analytics", description = "Returns hospital demand metrics, fulfillment success rates, and inventory consumption.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Analytics retrieved")
    })
    @GetMapping("/profile-analytics")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<HospitalAnalyticsResponse>> getAnalytics(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch hospital analytics for user: {}", userDetails.getUsername());
        ApiResponse<HospitalAnalyticsResponse> response = hospitalService.getAnalytics(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Blood Inventory Stock", description = "Retrieves blood bank stock inventory for all 8 blood groups.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blood inventory stock list retrieved")
    })
    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BloodInventoryResponse>>> getBloodInventory(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch blood inventory for user: {}", userDetails.getUsername());
        ApiResponse<List<BloodInventoryResponse>> response = hospitalService.getBloodInventory(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Blood Inventory Stock", description = "Updates available units and critical thresholds for a blood group.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blood inventory updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PutMapping("/inventory")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<BloodInventoryResponse>> updateInventory(
            @Valid @RequestBody UpdateInventoryRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to update blood inventory for user: {}", userDetails.getUsername());
        ApiResponse<BloodInventoryResponse> response = hospitalService.updateInventory(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create Emergency Blood Request", description = "Creates a new emergency blood request ticket.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Emergency blood request created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping({"/blood-requests", "/requests"})
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> createBloodRequest(
            @Valid @RequestBody HospitalBloodRequestCreate request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to create emergency blood request by hospital user: {}", userDetails.getUsername());
        ApiResponse<BloodRequestResponse> response = hospitalService.createBloodRequest(userDetails.getUsername(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get Hospital Blood Requests", description = "Retrieves all emergency blood requests associated with the hospital.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hospital blood requests retrieved")
    })
    @GetMapping({"/blood-requests", "/requests"})
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BloodRequestResponse>>> getHospitalBloodRequests(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to fetch hospital blood requests for user: {}", userDetails.getUsername());
        ApiResponse<List<BloodRequestResponse>> response = hospitalService.getHospitalBloodRequests(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Matched Donors for Request", description = "Retrieves compatible, available donors matched with the emergency blood request.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Matched donors retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blood request not found")
    })
    @GetMapping("/matched-donors/{requestId}")
    @PreAuthorize("hasRole('HOSPITAL')")
    public ResponseEntity<ApiResponse<List<DonorMatchViewResponse>>> getMatchedDonors(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to fetch matched donors for request ID: {} by user: {}", requestId, userDetails.getUsername());
        ApiResponse<List<DonorMatchViewResponse>> response = hospitalService.getMatchedDonors(userDetails.getUsername(), requestId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Emergency Request Donor Responses", description = "Retrieves matched donors and their response statuses (ACCEPTED, PENDING, REJECTED) for a specific emergency blood request.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Donor responses retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied: Hospital does not own request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blood request not found")
    })
    @GetMapping({"/emergency-requests/{requestId}/responses", "/requests/{requestId}/responses"})
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<com.bloodbridge.dto.response.HospitalEmergencyResponsesContainerDTO>> getEmergencyRequestResponses(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to fetch donor responses for blood request #{} by hospital user: {}", requestId, userDetails.getUsername());
        ApiResponse<com.bloodbridge.dto.response.HospitalEmergencyResponsesContainerDTO> response = hospitalService.getEmergencyRequestResponses(userDetails.getUsername(), requestId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirm Accepted Donor for Emergency Request", description = "Selects and confirms an accepted donor for an emergency blood request, moving request to fulfillment in progress.")
    @PostMapping({"/emergency-requests/{requestId}/confirm-donor/{matchedDonorId}", "/requests/{requestId}/confirm-donor/{matchedDonorId}"})
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<com.bloodbridge.dto.response.HospitalDonorResponseDTO>> confirmDonor(
            @PathVariable Long requestId,
            @PathVariable Long matchedDonorId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to confirm donor #{} for request #{} by hospital user: {}", matchedDonorId, requestId, userDetails.getUsername());
        ApiResponse<com.bloodbridge.dto.response.HospitalDonorResponseDTO> response = hospitalService.confirmDonor(userDetails.getUsername(), requestId, matchedDonorId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Start Emergency Request Fulfillment", description = "Transitions an emergency blood request status to FULFILLMENT_IN_PROGRESS.")
    @PostMapping({"/emergency-requests/{requestId}/start-fulfillment", "/requests/{requestId}/start-fulfillment"})
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> startFulfillment(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to start fulfillment for emergency blood request #{} by hospital user: {}", requestId, userDetails.getUsername());
        ApiResponse<BloodRequestResponse> response = hospitalService.startFulfillment(userDetails.getUsername(), requestId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Complete Emergency Blood Request", description = "Marks an emergency blood request and associated donor matches as COMPLETED.")
    @PostMapping({"/emergency-requests/{requestId}/complete", "/requests/{requestId}/complete"})
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> completeEmergencyRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to complete emergency blood request #{} by hospital user: {}", requestId, userDetails.getUsername());
        ApiResponse<BloodRequestResponse> response = hospitalService.completeEmergencyRequest(userDetails.getUsername(), requestId);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // ADMIN ENDPOINTS
    // ==========================================

    @Operation(summary = "Admin: Verify Hospital Registration", description = "Reviews and approves or rejects a hospital registration license.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hospital verification status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hospital not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.RequestMapping(value = "/{id}/verify", method = {org.springframework.web.bind.annotation.RequestMethod.PATCH, org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.PUT})
    public ResponseEntity<ApiResponse<HospitalResponse>> verifyHospital(
            @PathVariable Long id,
            @Parameter(description = "Target status (APPROVED or REJECTED)") @RequestParam String status,
            @Parameter(description = "Verification review remarks") @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String adminEmail = (userDetails != null && userDetails.getUsername() != null) ? userDetails.getUsername() : "admin@admin.com";
        log.info("Admin {} request to verify hospital ID: {} with status: {}", adminEmail, id, status);
        ApiResponse<HospitalResponse> response = hospitalService.verifyHospital(id, adminEmail, status, remarks);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Hospital Users", description = "Retrieves paginated list of registered users with search and filtering for authenticated hospitals.")
    @GetMapping({"/users", "/users/search"})
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<com.bloodbridge.dto.response.UserPageResponse>> getHospitalUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Hospital request to fetch users page: {}, size: {}, search: {}", page, size, search);
        com.bloodbridge.dto.response.UserPageResponse data = hospitalService.getAllUsers(search, bloodGroup, city, state, page, size);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", data));
    }

    @Operation(summary = "Get Hospital Donors", description = "Retrieves paginated list of registered donors with search, blood group, and availability filters for authenticated hospitals.")
    @GetMapping({"/donors", "/donors/search"})
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<com.bloodbridge.dto.response.DonorPageResponse>> getHospitalDonors(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Hospital request to fetch donors page: {}, size: {}, search: {}, available: {}", page, size, search, available);
        com.bloodbridge.dto.response.DonorPageResponse data = hospitalService.getAllDonors(search, bloodGroup, city, state, available, page, size);
        return ResponseEntity.ok(ApiResponse.success("Donors retrieved successfully", data));
    }
}
