package com.bloodbridge.service;

import com.bloodbridge.dto.request.AvailabilityRequest;
import com.bloodbridge.dto.request.CreateDonorProfileRequest;
import com.bloodbridge.dto.request.EmergencyAvailabilityRequest;
import com.bloodbridge.dto.request.PreferredRadiusRequest;
import com.bloodbridge.dto.request.UpdateDonorProfileRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.DonationHistoryResponse;
import com.bloodbridge.dto.response.DonorDashboardResponse;
import com.bloodbridge.dto.response.DonorProfileResponse;
import com.bloodbridge.dto.response.EligibilityResponse;

import java.util.List;

/**
 * Service interface for Donor Management & Smart Donor Portal workflows.
 */
public interface DonorProfileService {

    /**
     * Creates a new donor profile for an authenticated user with role DONOR.
     *
     * @param email authenticated user email
     * @param request profile creation request
     * @return ApiResponse containing DonorProfileResponse
     */
    ApiResponse<DonorProfileResponse> createProfile(String email, CreateDonorProfileRequest request);

    /**
     * Retrieves current donor profile.
     *
     * @param email authenticated user email
     * @return ApiResponse containing DonorProfileResponse
     */
    ApiResponse<DonorProfileResponse> getMyProfile(String email);

    /**
     * Updates current donor profile details.
     *
     * @param email authenticated user email
     * @param request profile update request
     * @return ApiResponse containing updated DonorProfileResponse
     */
    ApiResponse<DonorProfileResponse> updateProfile(String email, UpdateDonorProfileRequest request);

    /**
     * Toggles availability status for regular blood donation.
     *
     * @param email authenticated user email
     * @param request availability payload
     * @return ApiResponse containing updated DonorProfileResponse
     */
    ApiResponse<DonorProfileResponse> toggleAvailability(String email, AvailabilityRequest request);

    /**
     * Updates emergency availability status for urgent blood calls.
     *
     * @param email authenticated user email
     * @param request emergency availability payload
     * @return ApiResponse containing updated DonorProfileResponse
     */
    ApiResponse<DonorProfileResponse> updateEmergencyAvailability(String email, EmergencyAvailabilityRequest request);

    /**
     * Updates preferred donation distance radius.
     *
     * @param email authenticated user email
     * @param request preferred radius payload
     * @return ApiResponse containing updated DonorProfileResponse
     */
    ApiResponse<DonorProfileResponse> updatePreferredRadius(String email, PreferredRadiusRequest request);

    /**
     * Retrieves Smart Donor Dashboard metrics summary.
     *
     * @param email authenticated user email
     * @return ApiResponse containing DonorDashboardResponse
     */
    ApiResponse<DonorDashboardResponse> getDashboard(String email);

    /**
     * Calculates smart donor eligibility details, next eligible date, and health recommendations.
     *
     * @param email authenticated user email
     * @return ApiResponse containing EligibilityResponse
     */
    ApiResponse<EligibilityResponse> calculateEligibility(String email);

    /**
     * Retrieves donation history timeline for current donor.
     *
     * @param email authenticated user email
     * @return ApiResponse containing list of DonationHistoryResponse items
     */
    ApiResponse<List<DonationHistoryResponse>> getDonationHistory(String email);

    /**
     * Soft deletes (deactivates) donor profile.
     *
     * @param email authenticated user email
     * @return ApiResponse confirming profile deletion
     */
    ApiResponse<String> deleteProfile(String email);
}
