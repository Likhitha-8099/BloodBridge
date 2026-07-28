package com.bloodbridge.controller;

import com.bloodbridge.dto.CompatibilityResponse;
import com.bloodbridge.dto.DonorMatchResponse;
import com.bloodbridge.dto.MatchResponse;
import com.bloodbridge.dto.MatchingStatisticsResponse;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing endpoints for the Blood Matching Engine.
 */
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    /**
     * Searches for eligible, compatible donors for a verified request, ranked by compatibility score.
     * Restricted to HOSPITAL and ADMIN roles.
     *
     * @param requestId the blood request ID
     * @return ranked list of matching donors
     */
    @GetMapping("/request/{requestId}")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<List<DonorMatchResponse>> findMatchingDonors(@PathVariable Long requestId) {
        List<DonorMatchResponse> response = matchingService.findEligibleDonors(requestId);
        return ResponseEntity.ok(response);
    }

    /**
     * Resolves compatible donor blood groups for a given blood group.
     * Accessible by any authenticated user.
     *
     * @param bloodGroup the requested blood group enum
     * @return the compatibility mappings
     */
    @GetMapping("/compatibility/{bloodGroup}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CompatibilityResponse> getCompatibleBloodGroups(@PathVariable BloodGroup bloodGroup) {
        CompatibilityResponse response = matchingService.getCompatibleBloodGroups(bloodGroup);
        return ResponseEntity.ok(response);
    }

    /**
     * Generates and persists matching logs in the database.
     * Restricted to HOSPITAL and ADMIN roles.
     *
     * @param requestId the blood request ID
     * @return the list of created matches
     */
    @PostMapping("/request/{requestId}/generate")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<List<MatchResponse>> generateMatches(@PathVariable Long requestId) {
        List<MatchResponse> response = matchingService.generateMatches(requestId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all generated matches for a blood request.
     * Accessible by PATIENT (owner), HOSPITAL (assignee), or ADMIN.
     *
     * @param requestId the blood request ID
     * @return list of match response objects
     */
    @GetMapping("/request/{requestId}/results")
    @PreAuthorize("hasAnyRole('PATIENT', 'HOSPITAL', 'ADMIN')")
    public ResponseEntity<List<MatchResponse>> getMatchesForRequest(@PathVariable Long requestId) {
        List<MatchResponse> response = matchingService.getMatchesForRequest(requestId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all generated matches assigned to a donor.
     * Accessible by DONOR (owner) or ADMIN.
     *
     * @param donorId the donor profile ID
     * @return list of match response objects
     */
    @GetMapping("/donor/{donorId}")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<List<MatchResponse>> getMatchesForDonor(@PathVariable Long donorId) {
        List<MatchResponse> response = matchingService.getMatchesForDonor(donorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves matching statistics.
     * Restricted to the ADMIN role.
     *
     * @return statistics payload
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MatchingStatisticsResponse> getMatchingStatistics() {
        MatchingStatisticsResponse response = matchingService.getMatchingStatistics();
        return ResponseEntity.ok(response);
    }
}
