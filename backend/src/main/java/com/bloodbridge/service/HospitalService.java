package com.bloodbridge.service;

import com.bloodbridge.dto.request.CreateHospitalRequest;
import com.bloodbridge.dto.request.HospitalBloodRequestCreate;
import com.bloodbridge.dto.request.UpdateHospitalRequest;
import com.bloodbridge.dto.request.UpdateInventoryRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.BloodInventoryResponse;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.response.DonorMatchViewResponse;
import com.bloodbridge.dto.response.HospitalAnalyticsResponse;
import com.bloodbridge.dto.response.HospitalDashboardResponse;
import com.bloodbridge.dto.response.HospitalResponse;

import java.util.List;

/**
 * Service interface for Hospital Management, Blood Inventory & Emergency Request Center workflows.
 */
public interface HospitalService {

    /**
     * Creates a new hospital profile for an authenticated user with role HOSPITAL.
     *
     * @param email authenticated user email
     * @param request creation request
     * @return ApiResponse containing HospitalResponse
     */
    ApiResponse<HospitalResponse> createHospital(String email, CreateHospitalRequest request);

    /**
     * Retrieves current hospital profile.
     *
     * @param email authenticated user email
     * @return ApiResponse containing HospitalResponse
     */
    ApiResponse<HospitalResponse> getMyHospital(String email);

    /**
     * Updates hospital profile details.
     *
     * @param email authenticated user email
     * @param request update request
     * @return ApiResponse containing updated HospitalResponse
     */
    ApiResponse<HospitalResponse> updateHospital(String email, UpdateHospitalRequest request);

    /**
     * Uploads/updates medical license document URL.
     *
     * @param email authenticated user email
     * @param documentUrl public document URL
     * @return ApiResponse containing updated HospitalResponse
     */
    ApiResponse<HospitalResponse> uploadLicense(String email, String documentUrl);

    /**
     * Uploads/updates hospital logo URL.
     *
     * @param email authenticated user email
     * @param logoUrl public logo URL
     * @return ApiResponse containing updated HospitalResponse
     */
    ApiResponse<HospitalResponse> uploadLogo(String email, String logoUrl);

    /**
     * Retrieves Hospital Dashboard summary metrics.
     *
     * @param email authenticated user email
     * @return ApiResponse containing HospitalDashboardResponse
     */
    ApiResponse<HospitalDashboardResponse> getDashboard(String email);

    /**
     * Retrieves Hospital Analytics & Demand metrics.
     *
     * @param email authenticated user email
     * @return ApiResponse containing HospitalAnalyticsResponse
     */
    ApiResponse<HospitalAnalyticsResponse> getAnalytics(String email);

    /**
     * Retrieves current blood bank inventory stock list for the hospital.
     *
     * @param email authenticated user email
     * @return ApiResponse containing list of BloodInventoryResponse items
     */
    ApiResponse<List<BloodInventoryResponse>> getBloodInventory(String email);

    /**
     * Updates available stock units for a specific blood group.
     *
     * @param email authenticated user email
     * @param request inventory update payload
     * @return ApiResponse containing updated BloodInventoryResponse
     */
    ApiResponse<BloodInventoryResponse> updateInventory(String email, UpdateInventoryRequest request);

    /**
     * Creates a new Emergency Blood Request by hospital.
     *
     * @param email authenticated user email
     * @param request emergency blood request payload
     * @return ApiResponse containing BloodRequestResponse
     */
    ApiResponse<BloodRequestResponse> createBloodRequest(String email, HospitalBloodRequestCreate request);

    /**
     * Retrieves all emergency blood requests created by/associated with the hospital.
     *
     * @param email authenticated user email
     * @return ApiResponse containing list of BloodRequestResponse items
     */
    ApiResponse<List<BloodRequestResponse>> getHospitalBloodRequests(String email);

    /**
     * Retrieves matched compatible donors for a specific emergency blood request.
     *
     * @param email authenticated user email
     * @param requestId blood request ID
     * @return ApiResponse containing list of DonorMatchViewResponse items
     */
    ApiResponse<List<DonorMatchViewResponse>> getMatchedDonors(String email, Long requestId);

    /**
     * Admin: Reviews and verifies/rejects a hospital registration.
     *
     * @param hospitalId hospital ID
     * @param adminEmail admin email
     * @param status status ("APPROVED" / "REJECTED")
     * @param remarks review remarks
     * @return ApiResponse containing updated HospitalResponse
     */
    ApiResponse<HospitalResponse> verifyHospital(Long hospitalId, String adminEmail, String status, String remarks);

    /**
     * Retrieves paginated list of users for Hospital portal with filtering.
     */
    com.bloodbridge.dto.response.UserPageResponse getAllUsers(String search, String bloodGroup, String city, String state, int page, int size);

    /**
     * Retrieves paginated list of donors for Hospital portal with filtering.
     */
    com.bloodbridge.dto.response.DonorPageResponse getAllDonors(String search, String bloodGroup, String city, String state, Boolean available, int page, int size);

    /**
     * Retrieves matched donors and their response statuses for an emergency blood request.
     */
    ApiResponse<com.bloodbridge.dto.response.HospitalEmergencyResponsesContainerDTO> getEmergencyRequestResponses(String email, Long requestId);

    /**
     * Confirms an accepted matched donor for an emergency blood request.
     */
    ApiResponse<com.bloodbridge.dto.response.HospitalDonorResponseDTO> confirmDonor(String email, Long requestId, Long matchedDonorId);

    /**
     * Transitions an emergency request to FULFILLMENT_IN_PROGRESS.
     */
    ApiResponse<BloodRequestResponse> startFulfillment(String email, Long requestId);

    /**
     * Marks an emergency blood request as COMPLETED.
     */
    ApiResponse<BloodRequestResponse> completeEmergencyRequest(String email, Long requestId);
}
