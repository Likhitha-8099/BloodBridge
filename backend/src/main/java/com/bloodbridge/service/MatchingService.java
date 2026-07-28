package com.bloodbridge.service;

import com.bloodbridge.dto.CompatibilityResponse;
import com.bloodbridge.dto.DonorMatchResponse;
import com.bloodbridge.dto.MatchResponse;
import com.bloodbridge.dto.MatchingStatisticsResponse;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.enums.BloodGroup;

import java.util.List;

/**
 * Service interface for the Blood Matching Engine.
 */
public interface MatchingService {

    /**
     * Resolves compatible donor blood groups for a given blood group.
     *
     * @param bloodGroup the requested blood group
     * @return the compatibility details
     */
    CompatibilityResponse getCompatibleBloodGroups(BloodGroup bloodGroup);

    /**
     * Finds and ranks eligible, compatible donors for a verified blood request.
     *
     * @param requestId the request ID
     * @return a list of ranked donor matches
     */
    List<DonorMatchResponse> findEligibleDonors(Long requestId);

    /**
     * Generates and persists match results for a verified request.
     *
     * @param requestId the request ID
     * @return the generated match results
     */
    List<MatchResponse> generateMatches(Long requestId);

    /**
     * Ranks a list of donors against a blood request based on score calculations.
     *
     * @param donors  the list of donors
     * @param request the blood request
     * @return ranked donor responses
     */
    List<DonorMatchResponse> rankDonors(List<DonorProfile> donors, BloodRequest request);

    /**
     * Retrieves generated match results associated with a request.
     *
     * @param requestId the request ID
     * @return the list of matches
     */
    List<MatchResponse> getMatchesForRequest(Long requestId);

    /**
     * Retrieves match results assigned to a specific donor profile.
     *
     * @param donorId the donor profile ID
     * @return the list of matches
     */
    List<MatchResponse> getMatchesForDonor(Long donorId);

    /**
     * Calculates the compatibility scoring for a donor profile relative to a blood request.
     *
     * @param donor   the donor profile
     * @param request the blood request
     * @return the calculated score points
     */
    Integer calculateCompatibilityScore(DonorProfile donor, BloodRequest request);

    /**
     * Retrieves overall aggregate matching statistics.
     *
     * @return match statistics
     */
    MatchingStatisticsResponse getMatchingStatistics();
}
