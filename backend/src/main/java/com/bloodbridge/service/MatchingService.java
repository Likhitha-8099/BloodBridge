package com.bloodbridge.service;

import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.MatchDashboardResponse;
import com.bloodbridge.dto.response.MatchResponse;

import java.util.List;

/**
 * Service interface for Intelligent Blood Matching & Allocation Engine workflows.
 */
public interface MatchingService {

    /**
     * Triggers the matching engine for a specific blood request, persists MatchResult entities, and returns top ranked matches.
     *
     * @param bloodRequestId target blood request ID
     * @return ApiResponse containing list of MatchResponse items
     */
    ApiResponse<List<MatchResponse>> triggerMatching(Long bloodRequestId);

    /**
     * Retrieves existing match results for a blood request ordered by rank.
     *
     * @param bloodRequestId blood request ID
     * @return ApiResponse containing list of MatchResponse items
     */
    ApiResponse<List<MatchResponse>> getMatchesByRequestId(Long bloodRequestId);

    /**
     * Retrieves all match results across requests.
     *
     * @return ApiResponse containing list of MatchResponse items
     */
    ApiResponse<List<MatchResponse>> getAllMatches();

    /**
     * Recalculates and replaces matches for a blood request (e.g. when availability or request details change).
     *
     * @param bloodRequestId blood request ID
     * @return ApiResponse containing list of updated MatchResponse items
     */
    ApiResponse<List<MatchResponse>> recalculateMatches(Long bloodRequestId);

    /**
     * Retrieves hospital match dashboard overview for a blood request.
     *
     * @param bloodRequestId blood request ID
     * @return ApiResponse containing MatchDashboardResponse
     */
    ApiResponse<MatchDashboardResponse> getMatchDashboard(Long bloodRequestId);
}
