package com.bloodbridge.controller;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.DonationResponse;
import com.bloodbridge.dto.DonationStatisticsResponse;
import com.bloodbridge.dto.DonationSummaryResponse;
import com.bloodbridge.dto.DonationStatusUpdateRequest;
import com.bloodbridge.service.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for donation tracking and fulfillment.
 */
@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    /**
     * Donor accepts a match request, transitioning the state and creating a donation.
     * Restricted to the DONOR role.
     *
     * @param matchId the match result ID
     * @return the created donation details
     */
    @PostMapping("/accept/{matchId}")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<DonationResponse> acceptDonation(@PathVariable Long matchId) {
        DonationResponse response = donationService.acceptDonation(matchId);
        return ResponseEntity.ok(response);
    }

    /**
     * Donor rejects a match request. Restricted to the DONOR role.
     *
     * @param matchId the match result ID
     * @return status confirmation response
     */
    @PostMapping("/reject/{matchId}")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<ApiResponse> rejectDonation(@PathVariable Long matchId) {
        ApiResponse response = donationService.rejectDonation(matchId);
        return ResponseEntity.ok(response);
    }

    /**
     * Hospital confirms the donor details for the scheduled donation.
     * Restricted to the HOSPITAL role.
     *
     * @param id the donation ID
     * @return updated donation details
     */
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('HOSPITAL')")
    public ResponseEntity<DonationResponse> confirmDonation(@PathVariable Long id) {
        DonationResponse response = donationService.confirmDonation(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Hospital completes a donation record, specifying units and remarks.
     * Restricted to the HOSPITAL role.
     *
     * @param id      the donation ID
     * @param request final units and remarks
     * @return completed donation details
     */
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('HOSPITAL')")
    public ResponseEntity<DonationResponse> completeDonation(
            @PathVariable Long id,
            @Valid @RequestBody DonationStatusUpdateRequest request
    ) {
        DonationResponse response = donationService.completeDonation(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancels a pending or confirmed donation.
     * Restricted to DONOR, HOSPITAL, or ADMIN.
     *
     * @param id the donation ID
     * @return cancelled donation details
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DONOR', 'HOSPITAL', 'ADMIN')")
    public ResponseEntity<DonationResponse> cancelDonation(@PathVariable Long id) {
        DonationResponse response = donationService.cancelDonation(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves detailed donation info. Accessible by roles involved or ADMIN.
     *
     * @param id the donation ID
     * @return donation details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DONOR', 'PATIENT', 'HOSPITAL', 'ADMIN')")
    public ResponseEntity<DonationResponse> getDonationById(@PathVariable Long id) {
        DonationResponse response = donationService.getDonationById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves donation history for a specific donor. Accessible by DONOR (own) or ADMIN.
     *
     * @param donorId the donor profile ID
     * @return list of summaries
     */
    @GetMapping("/donor/{donorId}")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<List<DonationSummaryResponse>> getDonationsByDonor(@PathVariable Long donorId) {
        List<DonationSummaryResponse> response = donationService.getDonationsByDonor(donorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves donation history for a patient. Accessible by PATIENT (own) or ADMIN.
     *
     * @param patientId the patient profile ID
     * @return list of summaries
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<List<DonationSummaryResponse>> getDonationsByPatient(@PathVariable Long patientId) {
        List<DonationSummaryResponse> response = donationService.getDonationsByPatient(patientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves donations hosted by a hospital. Accessible by HOSPITAL (own) or ADMIN.
     *
     * @param hospitalId the hospital ID
     * @return list of summaries
     */
    @GetMapping("/hospital/{hospitalId}")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<List<DonationSummaryResponse>> getDonationsByHospital(@PathVariable Long hospitalId) {
        List<DonationSummaryResponse> response = donationService.getDonationsByHospital(hospitalId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all donation history records. Restricted to the ADMIN role.
     *
     * @return list of summaries
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DonationSummaryResponse>> getDonationHistory() {
        List<DonationSummaryResponse> response = donationService.getDonationHistory();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves donation analytics and trends. Restricted to the ADMIN role.
     *
     * @return statistics response
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DonationStatisticsResponse> getDonationStatistics() {
        DonationStatisticsResponse response = donationService.getDonationStatistics();
        return ResponseEntity.ok(response);
    }
}
