package com.bloodbridge.controller;

import com.bloodbridge.dto.request.AcceptEmergencyRequestDTO;
import com.bloodbridge.dto.request.RejectEmergencyRequestDTO;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.EmergencyResponseDTO;
import com.bloodbridge.service.EmergencyResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for handling donor responses (Accept/Reject) to emergency blood requests.
 */
@RestController
@RequestMapping({"/api/v1/donor/emergency-responses", "/api/donor/emergency-responses"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Donor Emergency Responses", description = "Endpoints for donors to accept, reject, and view emergency blood requests")
public class EmergencyResponseController {

    private final EmergencyResponseService emergencyResponseService;

    @Operation(summary = "Accept Emergency Request", description = "Donor accepts an emergency request, provides ETA, and receives Google Maps navigation URL.")
    @PostMapping("/accept")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<EmergencyResponseDTO>> acceptEmergencyRequest(
            Authentication authentication,
            @Valid @RequestBody AcceptEmergencyRequestDTO dto) {

        String donorEmail = authentication.getName();
        log.info("[CONTROLLER-ACCEPT] Donor {} accepting Emergency Request #{}", donorEmail, dto.getEmergencyRequestId());

        EmergencyResponseDTO responseDTO = emergencyResponseService.acceptEmergencyRequest(donorEmail, dto);
        return ResponseEntity.ok(ApiResponse.success("Emergency blood request accepted successfully", responseDTO));
    }

    @Operation(summary = "Reject Emergency Request", description = "Donor declines an emergency blood request.")
    @PostMapping("/reject")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<EmergencyResponseDTO>> rejectEmergencyRequest(
            Authentication authentication,
            @Valid @RequestBody RejectEmergencyRequestDTO dto) {

        String donorEmail = authentication.getName();
        log.info("[CONTROLLER-REJECT] Donor {} rejecting Emergency Request #{}", donorEmail, dto.getEmergencyRequestId());

        EmergencyResponseDTO responseDTO = emergencyResponseService.rejectEmergencyRequest(donorEmail, dto);
        return ResponseEntity.ok(ApiResponse.success("Emergency blood request rejected", responseDTO));
    }

    @Operation(summary = "Get My Emergency Responses", description = "Retrieves all emergency request responses submitted by the logged-in donor.")
    @GetMapping("/my-responses")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<EmergencyResponseDTO>>> getMyResponses(Authentication authentication) {
        String donorEmail = authentication.getName();
        List<EmergencyResponseDTO> responses = emergencyResponseService.getMyResponses(donorEmail);
        return ResponseEntity.ok(ApiResponse.success("Donor emergency responses retrieved successfully", responses));
    }

    @PostMapping("/{requestId}/start-travel")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<EmergencyResponseDTO>> startTravel(Authentication authentication, @PathVariable Long requestId) {
        EmergencyResponseDTO dto = emergencyResponseService.startTravel(authentication.getName(), requestId);
        return ResponseEntity.ok(ApiResponse.success("Donor travel status updated to STARTED_TRAVEL", dto));
    }

    @PostMapping("/{requestId}/reached-hospital")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<EmergencyResponseDTO>> reachHospital(Authentication authentication, @PathVariable Long requestId) {
        EmergencyResponseDTO dto = emergencyResponseService.reachHospital(authentication.getName(), requestId);
        return ResponseEntity.ok(ApiResponse.success("Donor travel status updated to REACHED_HOSPITAL", dto));
    }

    @PostMapping("/{requestId}/complete-donation")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<EmergencyResponseDTO>> completeDonation(Authentication authentication, @PathVariable Long requestId) {
        EmergencyResponseDTO dto = emergencyResponseService.completeDonation(authentication.getName(), requestId);
        return ResponseEntity.ok(ApiResponse.success("Donation completed and reward generated successfully", dto));
    }
}
