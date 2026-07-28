package com.bloodbridge.service;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.DonationResponse;
import com.bloodbridge.dto.DonationStatisticsResponse;
import com.bloodbridge.dto.DonationSummaryResponse;
import com.bloodbridge.dto.DonationStatusUpdateRequest;
import com.bloodbridge.entity.DonorProfile;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface defining donation tracking workflows.
 */
public interface DonationService {

    /**
     * Executes donor accepting a matching request.
     * Creates a new donation record in ACCEPTED status.
     *
     * @param matchId the match result ID
     * @return the created donation details
     */
    DonationResponse acceptDonation(Long matchId);

    /**
     * Executes donor rejecting a matching request.
     * Updates match status to REJECTED.
     *
     * @param matchId the match result ID
     * @return generic status response
     */
    ApiResponse rejectDonation(Long matchId);

    /**
     * Hospital confirms the donor for donation, scheduling it.
     * Updates status to CONFIRMED.
     *
     * @param id the donation ID
     * @return updated donation details
     */
    DonationResponse confirmDonation(Long id);

    /**
     * Hospital records the completion of a donation transaction.
     * Updates status to COMPLETED, record units donated and remarks, and updates donor stats.
     * Also checks and updates blood request completion if required units are met.
     *
     * @param id      the donation ID
     * @param request final units and remarks
     * @return updated donation details
     */
    DonationResponse completeDonation(Long id, DonationStatusUpdateRequest request);

    /**
     * Cancels a pending or accepted donation.
     * Updates status to CANCELLED.
     *
     * @param id the donation ID
     * @return updated donation details
     */
    DonationResponse cancelDonation(Long id);

    /**
     * Fetches a donation record by ID.
     *
     * @param id the donation ID
     * @return detailed donation response
     */
    DonationResponse getDonationById(Long id);

    /**
     * Fetches donation summaries for a specific donor.
     *
     * @param donorId the donor profile ID
     * @return list of donation summaries
     */
    List<DonationSummaryResponse> getDonationsByDonor(Long donorId);

    /**
     * Fetches donation summaries for a specific patient.
     *
     * @param patientId the patient profile ID
     * @return list of donation summaries
     */
    List<DonationSummaryResponse> getDonationsByPatient(Long patientId);

    /**
     * Fetches donation summaries hosted by a specific hospital.
     *
     * @param hospitalId the hospital ID
     * @return list of donation summaries
     */
    List<DonationSummaryResponse> getDonationsByHospital(Long hospitalId);

    /**
     * Fetches all donation records in the system. Restricted to ADMIN.
     *
     * @return list of all donation summaries
     */
    List<DonationSummaryResponse> getDonationHistory();

    /**
     * Computes donation statistics and analytical trends.
     *
     * @return donation statistics response
     */
    DonationStatisticsResponse getDonationStatistics();

    /**
     * Helper to update donor statistics (total donations, last donation date, availability).
     *
     * @param donor        the donor profile
     * @param donationDate the date of donation
     */
    void updateDonorStatistics(DonorProfile donor, LocalDate donationDate);
}
