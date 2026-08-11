package com.bloodbridge.controller;

import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.DonorEmergencyRequestDTO;
import com.bloodbridge.service.BloodRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Donor Emergency Blood Requests and Acceptance workflows.
 * Endpoint mappings support both legacy (/api/donor/...) and versioned (/api/v1/donor/...) paths.
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/donor", "/api/donor"})
@RequiredArgsConstructor
@Tag(name = "Donor Emergency Workflow", description = "Endpoints for donor emergency requests, accepting, and declining blood requests")
public class DonorEmergencyController {

    private final BloodRequestService bloodRequestService;

    @Operation(summary = "Get Matched Emergency Blood Requests for Donor", description = "Retrieves matched emergency blood requests assigned to the logged-in donor.")
    @GetMapping("/emergency-requests")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DonorEmergencyRequestDTO>>> getEmergencyRequests() {
        log.info("Request to fetch matched emergency blood requests for authenticated donor");
        List<DonorEmergencyRequestDTO> response = bloodRequestService.getMatchedEmergencyRequestsForDonor();
        return ResponseEntity.ok(ApiResponse.success("Matched emergency blood requests retrieved successfully", response));
    }

    @Operation(summary = "Accept Emergency Blood Request", description = "Donor accepts an emergency blood request by ID.")
    @PostMapping({"/emergency-requests/{id}/accept", "/requests/{id}/accept"})
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<DonorEmergencyRequestDTO>> acceptRequest(@PathVariable Long id) {
        log.info("Request by donor to accept emergency blood request #{}", id);
        DonorEmergencyRequestDTO response = bloodRequestService.acceptMatchedEmergencyRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Emergency blood request accepted successfully", response));
    }

    @Operation(summary = "Reject Emergency Blood Request", description = "Donor declines an emergency blood request by ID.")
    @PostMapping({"/emergency-requests/{id}/reject", "/requests/{id}/reject"})
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<DonorEmergencyRequestDTO>> rejectRequest(@PathVariable Long id) {
        log.info("Request by donor to decline emergency blood request #{}", id);
        DonorEmergencyRequestDTO response = bloodRequestService.rejectMatchedEmergencyRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Emergency blood request declined successfully", response));
    }
}
