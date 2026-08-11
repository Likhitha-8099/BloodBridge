package com.bloodbridge.controller;

import com.bloodbridge.dto.BloodRequestCreateRequest;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.BloodRequestSummaryResponse;
import com.bloodbridge.dto.BloodRequestUpdateRequest;
import com.bloodbridge.dto.RequestStatusResponse;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.service.BloodRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing blood requests.
 * Endpoint access is secured using method-level Spring Security annotations.
 */
@RestController
@RequestMapping({"/api/v1/requests", "/api/requests"})
@RequiredArgsConstructor
public class BloodRequestController {

    private final BloodRequestService bloodRequestService;

    /**
     * Creates a new blood request. Restricted to the 'PATIENT' role.
     *
     * @param request the request creation payload
     * @return the created request with status 201 (Created)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'HOSPITAL', 'ADMIN')")
    public ResponseEntity<BloodRequestResponse> createRequest(@Valid @RequestBody BloodRequestCreateRequest request) {
        BloodRequestResponse response = bloodRequestService.createRequest(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a blood request by its ID. Open to all authenticated users.
     *
     * @param id the request ID
     * @return the detailed blood request with status 200 (OK)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT', 'HOSPITAL', 'DONOR')")
    public ResponseEntity<BloodRequestResponse> getRequestById(@PathVariable Long id) {
        BloodRequestResponse response = bloodRequestService.getRequestById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all blood requests registered by the logged-in patient. Restricted to 'PATIENT'.
     *
     * @return a list of request summaries with status 200 (OK)
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<BloodRequestSummaryResponse>> getMyRequests() {
        List<BloodRequestSummaryResponse> response = bloodRequestService.getMyRequests();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all blood requests in the system. Restricted to 'ADMIN'.
     *
     * @return a list of all request summaries with status 200 (OK)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BloodRequestSummaryResponse>> getAllRequests() {
        List<BloodRequestSummaryResponse> response = bloodRequestService.getAllRequests();
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing blood request. Restricted to the 'PATIENT' who owns the request.
     *
     * @param id      the request ID
     * @param request the updated request payload
     * @return the updated blood request details with status 200 (OK)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<BloodRequestResponse> updateRequest(
            @PathVariable Long id,
            @Valid @RequestBody BloodRequestUpdateRequest request
    ) {
        BloodRequestResponse response = bloodRequestService.updateRequest(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancels a blood request. Restricted to the 'PATIENT' who owns the request.
     *
     * @param id the request ID
     * @return the updated request details showing CANCELLED status with status 200 (OK)
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<BloodRequestResponse> cancelRequest(@PathVariable Long id) {
        BloodRequestResponse response = bloodRequestService.cancelRequest(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Verifies a pending request. Restricted to the assigned 'HOSPITAL'.
     *
     * @param id the request ID
     * @return the status confirmation response with status 200 (OK)
     */
    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('HOSPITAL')")
    public ResponseEntity<RequestStatusResponse> verifyRequest(@PathVariable Long id) {
        RequestStatusResponse response = bloodRequestService.verifyRequest(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Rejects a pending request. Restricted to the assigned 'HOSPITAL'.
     *
     * @param id the request ID
     * @return the status confirmation response with status 200 (OK)
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('HOSPITAL')")
    public ResponseEntity<RequestStatusResponse> rejectRequest(@PathVariable Long id) {
        RequestStatusResponse response = bloodRequestService.rejectRequest(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Filters blood requests by status. Accessible by ADMIN, HOSPITAL, and DONOR roles.
     *
     * @param status the request status enum
     * @return a list of matching request summaries with status 200 (OK)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'DONOR')")
    public ResponseEntity<List<BloodRequestSummaryResponse>> getRequestsByStatus(@PathVariable RequestStatus status) {
        List<BloodRequestSummaryResponse> response = bloodRequestService.getRequestsByStatus(status);
        return ResponseEntity.ok(response);
    }

    /**
     * Filters blood requests by blood group needed. Accessible by ADMIN, HOSPITAL, and DONOR.
     *
     * @param bloodGroup the blood group enum
     * @return a list of matching request summaries with status 200 (OK)
     */
    @GetMapping("/blood-group/{bloodGroup}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'DONOR')")
    public ResponseEntity<List<BloodRequestSummaryResponse>> getRequestsByBloodGroup(@PathVariable BloodGroup bloodGroup) {
        List<BloodRequestSummaryResponse> response = bloodRequestService.getRequestsByBloodGroup(bloodGroup);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves active requests (PENDING or VERIFIED). Accessible by ADMIN, HOSPITAL, DONOR, and PATIENT.
     *
     * @return a list of active request summaries with status 200 (OK)
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'DONOR', 'PATIENT')")
    public ResponseEntity<List<BloodRequestSummaryResponse>> getActiveRequests() {
        List<BloodRequestSummaryResponse> response = bloodRequestService.getActiveRequests();
        return ResponseEntity.ok(response);
    }
}
