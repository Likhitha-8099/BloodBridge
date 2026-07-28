package com.bloodbridge.controller;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.HospitalRequest;
import com.bloodbridge.dto.HospitalResponse;
import com.bloodbridge.dto.HospitalSummaryResponse;
import com.bloodbridge.dto.HospitalVerificationResponse;
import com.bloodbridge.service.HospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing hospital profiles and verifications.
 * Access mappings are secured utilizing method-level Spring Security permissions.
 */
@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    /**
     * Creates a new hospital profile. Restricted to the 'HOSPITAL' role.
     *
     * @param request the profile details
     * @return the created hospital profile with status 201 (Created)
     */
    @PostMapping
    @PreAuthorize("hasRole('HOSPITAL')")
    public ResponseEntity<HospitalResponse> createHospital(@Valid @RequestBody HospitalRequest request) {
        HospitalResponse response = hospitalService.createHospital(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves the profile of the currently logged-in hospital. Restricted to 'HOSPITAL'.
     *
     * @return the hospital profile with status 200 (OK)
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('HOSPITAL')")
    public ResponseEntity<HospitalResponse> getMyHospital() {
        HospitalResponse response = hospitalService.getMyHospital();
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the profile of the currently logged-in hospital. Restricted to 'HOSPITAL'.
     *
     * @param request the updated profile details
     * @return the updated hospital profile with status 200 (OK)
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('HOSPITAL')")
    public ResponseEntity<HospitalResponse> updateHospital(@Valid @RequestBody HospitalRequest request) {
        HospitalResponse response = hospitalService.updateHospital(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes the profile of the currently logged-in hospital. Restricted to 'HOSPITAL'.
     *
     * @return a success response with status 200 (OK)
     */
    @DeleteMapping("/me")
    @PreAuthorize("hasRole('HOSPITAL')")
    public ResponseEntity<ApiResponse> deleteHospital() {
        ApiResponse response = hospitalService.deleteHospital();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a hospital profile by ID. Accessible by 'ADMIN', 'PATIENT', and 'DONOR' roles.
     *
     * @param id the hospital profile ID
     * @return the hospital profile with status 200 (OK)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT', 'DONOR')")
    public ResponseEntity<HospitalResponse> getHospitalById(@PathVariable Long id) {
        HospitalResponse response = hospitalService.getHospitalById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a summary list of all hospitals. Accessible by 'ADMIN', 'PATIENT', and 'DONOR'.
     *
     * @return a list of hospital summaries with status 200 (OK)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT', 'DONOR')")
    public ResponseEntity<List<HospitalSummaryResponse>> getAllHospitals() {
        List<HospitalSummaryResponse> response = hospitalService.getAllHospitals();
        return ResponseEntity.ok(response);
    }

    /**
     * Searches for hospitals by city name. Accessible by all authenticated roles.
     *
     * @param city the city name to search for
     * @return a list of matching hospital summaries with status 200 (OK)
     */
    @GetMapping("/search/city/{city}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT', 'DONOR', 'HOSPITAL')")
    public ResponseEntity<List<HospitalSummaryResponse>> searchByCity(@PathVariable String city) {
        List<HospitalSummaryResponse> response = hospitalService.searchByCity(city);
        return ResponseEntity.ok(response);
    }

    /**
     * Verifies a hospital profile by setting its verified status to true. Restricted to the 'ADMIN' role.
     *
     * @param id the hospital profile ID
     * @return the verification status response with status 200 (OK)
     */
    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HospitalVerificationResponse> verifyHospital(@PathVariable Long id) {
        HospitalVerificationResponse response = hospitalService.verifyHospital(id);
        return ResponseEntity.ok(response);
    }
}
