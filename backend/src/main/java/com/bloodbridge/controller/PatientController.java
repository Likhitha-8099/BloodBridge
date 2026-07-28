package com.bloodbridge.controller;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.PatientProfileRequest;
import com.bloodbridge.dto.PatientProfileResponse;
import com.bloodbridge.dto.PatientSummaryResponse;
import com.bloodbridge.service.PatientProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing patient profiles.
 * Provides endpoint access controls utilizing Spring Security method-level checks.
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientProfileService patientProfileService;

    /**
     * Creates a new patient profile. Restricted to users with the 'PATIENT' role.
     *
     * @param request the profile details
     * @return the created profile with status 201 (Created)
     */
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientProfileResponse> createProfile(@Valid @RequestBody PatientProfileRequest request) {
        PatientProfileResponse response = patientProfileService.createProfile(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves the profile of the currently logged-in patient. Restricted to the 'PATIENT' role.
     *
     * @return the profile details with status 200 (OK)
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientProfileResponse> getMyProfile() {
        PatientProfileResponse response = patientProfileService.getMyProfile();
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the profile of the currently logged-in patient. Restricted to the 'PATIENT' role.
     *
     * @param request the updated profile details
     * @return the updated profile with status 200 (OK)
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientProfileResponse> updateProfile(@Valid @RequestBody PatientProfileRequest request) {
        PatientProfileResponse response = patientProfileService.updateProfile(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes the profile of the currently logged-in patient. Restricted to the 'PATIENT' role.
     *
     * @return a success response with status 200 (OK)
     */
    @DeleteMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse> deleteProfile() {
        ApiResponse response = patientProfileService.deleteProfile();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a patient's profile by their profile ID.
     * Accessible by 'ADMIN' and 'HOSPITAL' roles.
     *
     * @param id the patient profile ID
     * @return the detailed patient profile with status 200 (OK)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL')")
    public ResponseEntity<PatientProfileResponse> getPatientById(@PathVariable Long id) {
        PatientProfileResponse response = patientProfileService.getPatientById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all patient profiles in the system. Restricted to the 'ADMIN' role.
     *
     * @return a list of patient profile summaries with status 200 (OK)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PatientSummaryResponse>> getAllPatients() {
        List<PatientSummaryResponse> response = patientProfileService.getAllPatients();
        return ResponseEntity.ok(response);
    }
}
