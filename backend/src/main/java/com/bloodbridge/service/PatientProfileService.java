package com.bloodbridge.service;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.PatientProfileRequest;
import com.bloodbridge.dto.PatientProfileResponse;
import com.bloodbridge.dto.PatientSummaryResponse;

import java.util.List;

/**
 * Service interface defining workflows for managing patient profiles.
 */
public interface PatientProfileService {

    /**
     * Creates a new patient profile for the currently authenticated user.
     *
     * @param request the profile details
     * @return the created profile response
     */
    PatientProfileResponse createProfile(PatientProfileRequest request);

    /**
     * Retrieves the profile of the currently authenticated patient.
     *
     * @return the patient's profile details
     */
    PatientProfileResponse getMyProfile();

    /**
     * Updates the profile of the currently authenticated patient.
     *
     * @param request the updated details
     * @return the updated profile response
     */
    PatientProfileResponse updateProfile(PatientProfileRequest request);

    /**
     * Deletes the profile of the currently authenticated patient.
     *
     * @return an {@link ApiResponse} confirming deletion status
     */
    ApiResponse deleteProfile();

    /**
     * Retrieves a patient's profile by their profile ID.
     * Typically accessible by ADMIN or HOSPITAL roles.
     *
     * @param id the patient profile ID
     * @return the detailed patient profile response
     */
    PatientProfileResponse getPatientById(Long id);

    /**
     * Retrieves a summary of all patient profiles in the system.
     * Restricted to ADMIN role.
     *
     * @return a list of all patient profiles
     */
    List<PatientSummaryResponse> getAllPatients();
}
