package com.bloodbridge.service;

import com.bloodbridge.dto.BloodRequestCreateRequest;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.BloodRequestSummaryResponse;
import com.bloodbridge.dto.BloodRequestUpdateRequest;
import com.bloodbridge.dto.RequestStatusResponse;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.RequestStatus;

import java.util.List;

/**
 * Service interface defining workflows for managing blood requests.
 */
public interface BloodRequestService {

    /**
     * Creates a new blood request. Restricted to the 'PATIENT' role.
     *
     * @param request the request creation details
     * @return the created blood request response
     */
    BloodRequestResponse createRequest(BloodRequestCreateRequest request);

    /**
     * Retrieves a blood request by its ID.
     *
     * @param id the request ID
     * @return the detailed blood request response
     */
    BloodRequestResponse getRequestById(Long id);

    /**
     * Retrieves all blood requests registered by the currently logged-in patient.
     *
     * @return a list of blood request summaries
     */
    List<BloodRequestSummaryResponse> getMyRequests();

    /**
     * Updates an existing blood request. Restricted to 'PATIENT' own requests.
     * Only uncompleted and uncancelled requests can be edited.
     *
     * @param id      the request ID
     * @param request the updated request details
     * @return the updated blood request response
     */
    BloodRequestResponse updateRequest(Long id, BloodRequestUpdateRequest request);

    /**
     * Cancels a blood request. Restricted to 'PATIENT' own requests.
     *
     * @param id the request ID
     * @return the cancelled blood request response
     */
    BloodRequestResponse cancelRequest(Long id);

    /**
     * Verifies a blood request. Restricted to the assigned 'HOSPITAL'.
     *
     * @param id the request ID
     * @return the status transition response
     */
    RequestStatusResponse verifyRequest(Long id);

    /**
     * Rejects a blood request. Restricted to the assigned 'HOSPITAL'.
     *
     * @param id the request ID
     * @return the status transition response
     */
    RequestStatusResponse rejectRequest(Long id);

    /**
     * Retrieves all blood requests in the system. Restricted to 'ADMIN'.
     *
     * @return a list of all request summaries
     */
    List<BloodRequestSummaryResponse> getAllRequests();

    /**
     * Filters blood requests by status.
     *
     * @param status the request status
     * @return a list of matching request summaries
     */
    List<BloodRequestSummaryResponse> getRequestsByStatus(RequestStatus status);

    /**
     * Filters blood requests by blood group needed.
     *
     * @param bloodGroup the blood group needed
     * @return a list of matching request summaries
     */
    List<BloodRequestSummaryResponse> getRequestsByBloodGroup(BloodGroup bloodGroup);

    /**
     * Retrieves all active requests in the system (e.g. status PENDING or VERIFIED).
     *
     * @return a list of active request summaries
     */
    List<BloodRequestSummaryResponse> getActiveRequests();

    /**
     * Retrieves emergency requests matching authenticated donor's blood group, city, and state.
     * Sorted highest urgency first, then latest request.
     */
    List<BloodRequestSummaryResponse> getEmergencyRequestsForDonor();

    /**
     * Retrieves matched emergency blood requests assigned to the logged-in donor by the Smart Donor Matching Engine.
     */
    List<com.bloodbridge.dto.response.DonorEmergencyRequestDTO> getMatchedEmergencyRequestsForDonor();

    /**
     * Accepts a blood request by ID for authenticated donor.
     * Prevents duplicate acceptances and notifies the hospital.
     */
    BloodRequestSummaryResponse acceptBloodRequest(Long id);

    /**
     * Accepts a matched emergency blood request by ID for authenticated donor.
     */
    com.bloodbridge.dto.response.DonorEmergencyRequestDTO acceptMatchedEmergencyRequest(Long bloodRequestId);

    /**
     * Rejects/declines a blood request by ID for authenticated donor.
     */
    BloodRequestSummaryResponse rejectBloodRequest(Long id);

    /**
     * Rejects/declines a matched emergency blood request by ID for authenticated donor.
     */
    com.bloodbridge.dto.response.DonorEmergencyRequestDTO rejectMatchedEmergencyRequest(Long bloodRequestId);
}
