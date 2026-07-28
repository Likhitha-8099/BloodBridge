package com.bloodbridge.service;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.HospitalRequest;
import com.bloodbridge.dto.HospitalResponse;
import com.bloodbridge.dto.HospitalSummaryResponse;
import com.bloodbridge.dto.HospitalVerificationResponse;

import java.util.List;

/**
 * Service interface defining workflows for managing hospital profiles.
 */
public interface HospitalService {

    /**
     * Creates a new hospital profile for the currently authenticated user.
     *
     * @param request the hospital request details
     * @return the created hospital profile response
     */
    HospitalResponse createHospital(HospitalRequest request);

    /**
     * Retrieves the profile of the currently authenticated hospital.
     *
     * @return the hospital's profile details
     */
    HospitalResponse getMyHospital();

    /**
     * Updates the profile of the currently authenticated hospital.
     *
     * @param request the updated details
     * @return the updated hospital profile response
     */
    HospitalResponse updateHospital(HospitalRequest request);

    /**
     * Deletes the profile of the currently authenticated hospital.
     *
     * @return an {@link ApiResponse} confirming deletion status
     */
    ApiResponse deleteHospital();

    /**
     * Retrieves a hospital's profile by its profile ID.
     *
     * @param id the hospital profile ID
     * @return the detailed hospital response
     */
    HospitalResponse getHospitalById(Long id);

    /**
     * Retrieves a summary of all hospital profiles in the system.
     *
     * @return a list of all hospital profiles
     */
    List<HospitalSummaryResponse> getAllHospitals();

    /**
     * Searches for hospitals located in a specific city.
     *
     * @param city the city name to search for
     * @return a list of matching hospital summaries
     */
    List<HospitalSummaryResponse> searchByCity(String city);

    /**
     * Verifies a hospital profile by setting its verified status to true.
     * Restricted to ADMIN role.
     *
     * @param id the hospital profile ID
     * @return a confirmation response including the verification status
     */
    HospitalVerificationResponse verifyHospital(Long id);
}
