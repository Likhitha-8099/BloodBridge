package com.bloodbridge.service;

import com.bloodbridge.dto.request.CreateBloodRequestRequest;
import com.bloodbridge.dto.request.CreatePatientProfileRequest;
import com.bloodbridge.dto.request.UpdateBloodRequestRequest;
import com.bloodbridge.dto.request.UpdatePatientProfileRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.response.PatientDashboardResponse;
import com.bloodbridge.dto.response.PatientProfileResponse;
import com.bloodbridge.dto.response.RequestTimelineResponse;

import java.util.List;

/**
 * Service interface for Patient Management & Emergency Blood Request Portal workflows.
 */
public interface PatientProfileService {

    /**
     * Creates a new patient profile for an authenticated user with role PATIENT.
     *
     * @param email authenticated user email
     * @param request creation request
     * @return ApiResponse containing PatientProfileResponse
     */
    ApiResponse<PatientProfileResponse> createProfile(String email, CreatePatientProfileRequest request);

    /**
     * Retrieves current patient profile.
     *
     * @param email authenticated user email
     * @return ApiResponse containing PatientProfileResponse
     */
    ApiResponse<PatientProfileResponse> getMyProfile(String email);

    /**
     * Updates patient profile details.
     *
     * @param email authenticated user email
     * @param request update request
     * @return ApiResponse containing updated PatientProfileResponse
     */
    ApiResponse<PatientProfileResponse> updateProfile(String email, UpdatePatientProfileRequest request);

    /**
     * Soft deletes (deactivates) patient profile.
     *
     * @param email authenticated user email
     * @return ApiResponse confirming deletion status
     */
    ApiResponse<String> deleteProfile(String email);

    /**
     * Retrieves Patient Dashboard summary metrics.
     *
     * @param email authenticated user email
     * @return ApiResponse containing PatientDashboardResponse
     */
    ApiResponse<PatientDashboardResponse> getDashboard(String email);

    /**
     * Creates a new emergency blood request.
     *
     * @param email authenticated user email
     * @param request emergency blood request creation payload
     * @return ApiResponse containing BloodRequestResponse
     */
    ApiResponse<BloodRequestResponse> createBloodRequest(String email, CreateBloodRequestRequest request);

    /**
     * Retrieves all blood requests registered by current patient.
     *
     * @param email authenticated user email
     * @return ApiResponse containing list of BloodRequestResponse items
     */
    ApiResponse<List<BloodRequestResponse>> getMyBloodRequests(String email);

    /**
     * Retrieves a specific blood request by ID.
     *
     * @param email authenticated user email
     * @param requestId blood request ID
     * @return ApiResponse containing BloodRequestResponse
     */
    ApiResponse<BloodRequestResponse> getBloodRequestById(String email, Long requestId);

    /**
     * Updates an existing uncompleted blood request.
     *
     * @param email authenticated user email
     * @param requestId blood request ID
     * @param request update request payload
     * @return ApiResponse containing updated BloodRequestResponse
     */
    ApiResponse<BloodRequestResponse> updateBloodRequest(String email, Long requestId, UpdateBloodRequestRequest request);

    /**
     * Cancels an active blood request.
     *
     * @param email authenticated user email
     * @param requestId blood request ID
     * @return ApiResponse containing cancelled BloodRequestResponse
     */
    ApiResponse<BloodRequestResponse> cancelBloodRequest(String email, Long requestId);

    /**
     * Retrieves real-time status tracking timeline for a blood request.
     *
     * @param email authenticated user email
     * @param requestId blood request ID
     * @return ApiResponse containing RequestTimelineResponse
     */
    ApiResponse<RequestTimelineResponse> getBloodRequestTimeline(String email, Long requestId);
}
