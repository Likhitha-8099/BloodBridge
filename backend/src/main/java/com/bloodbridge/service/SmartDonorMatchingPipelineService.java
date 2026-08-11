package com.bloodbridge.service;

import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.SmartDonorMatchingPipelineDTO;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.MatchingAnalytics;

/**
 * Service interface for orchestrating the 10-Stage Enterprise Smart Donor Matching Pipeline.
 */
public interface SmartDonorMatchingPipelineService {

    /**
     * Executes the complete 10-stage Smart Donor Matching Pipeline for a blood request.
     *
     * @param request target blood request entity
     * @return SmartDonorMatchingPipelineDTO containing 4 distance tier groups, section telemetry, and metrics
     */
    SmartDonorMatchingPipelineDTO executePipeline(BloodRequest request);

    /**
     * Executes pipeline for a request ID.
     *
     * @param bloodRequestId target request ID
     * @return ApiResponse containing SmartDonorMatchingPipelineDTO
     */
    ApiResponse<SmartDonorMatchingPipelineDTO> executePipelineForRequestId(Long bloodRequestId);

    /**
     * Retrieves existing or calculated pipeline results for a request ID.
     *
     * @param bloodRequestId target request ID
     * @return ApiResponse containing SmartDonorMatchingPipelineDTO
     */
    ApiResponse<SmartDonorMatchingPipelineDTO> getPipelineResults(Long bloodRequestId);

    /**
     * Retrieves analytics metrics for a blood request ID.
     *
     * @param bloodRequestId target request ID
     * @return ApiResponse containing MatchingAnalytics
     */
    ApiResponse<MatchingAnalytics> getAnalyticsForRequestId(Long bloodRequestId);
}
