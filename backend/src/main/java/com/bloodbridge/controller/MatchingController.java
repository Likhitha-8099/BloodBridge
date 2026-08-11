package com.bloodbridge.controller;

import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.MatchDashboardResponse;
import com.bloodbridge.dto.response.MatchResponse;
import com.bloodbridge.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Enterprise REST controller for Intelligent Blood Matching & Allocation Engine endpoints under /api/v1/matching.
 */
@RestController
@RequestMapping({"/api/v1/matching", "/api/matching"})
@RequiredArgsConstructor
@Tag(name = "Intelligent Blood Matching Module", description = "Endpoints for Triggering Strategy-Based Matching Engine, Ranked Matches, Recalculations, and Match Dashboard")
public class MatchingController {

    private static final Logger log = LoggerFactory.getLogger(MatchingController.class);

    private final MatchingService matchingService;

    @Operation(summary = "Trigger Matching Engine", description = "Executes Strategy Pattern matching engine for a blood request, scores donors, and persists top ranked matches.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Matching engine executed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blood request not found")
    })
    @PostMapping("/match/{bloodRequestId}")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'PATIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> triggerMatching(@PathVariable Long bloodRequestId) {
        log.info("Request to trigger matching engine for blood request ID: {}", bloodRequestId);
        ApiResponse<List<MatchResponse>> response = matchingService.triggerMatching(bloodRequestId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Matches for Request", description = "Retrieves ranked match results for a specific blood request ordered by rank position.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ranked match results retrieved")
    })
    @GetMapping("/match/{bloodRequestId}")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'PATIENT', 'DONOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getMatchesByRequestId(@PathVariable Long bloodRequestId) {
        log.info("Request to fetch matches for blood request ID: {}", bloodRequestId);
        ApiResponse<List<MatchResponse>> response = matchingService.getMatchesByRequestId(bloodRequestId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get All Matches", description = "Retrieves all match results in the system.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All matches retrieved")
    })
    @GetMapping("/matches")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getAllMatches() {
        log.info("Request to fetch all system match results");
        ApiResponse<List<MatchResponse>> response = matchingService.getAllMatches();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Recalculate Matches", description = "Recalculates and replaces matches when donor availability or request details change.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Matches recalculated successfully")
    })
    @PostMapping("/recalculate/{bloodRequestId}")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> recalculateMatches(@PathVariable Long bloodRequestId) {
        log.info("Request to recalculate matches for blood request ID: {}", bloodRequestId);
        ApiResponse<List<MatchResponse>> response = matchingService.recalculateMatches(bloodRequestId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Match Dashboard", description = "Retrieves hospital match overview dashboard with top ranked donors and request details.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Match dashboard retrieved")
    })
    @GetMapping("/dashboard/{bloodRequestId}")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<MatchDashboardResponse>> getMatchDashboard(@PathVariable Long bloodRequestId) {
        log.info("Request to fetch match dashboard for blood request ID: {}", bloodRequestId);
        ApiResponse<MatchDashboardResponse> response = matchingService.getMatchDashboard(bloodRequestId);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Phase 3D.1 Enterprise Smart Donor Matching Pipeline Endpoints
    // ─────────────────────────────────────────────────────────────────────────────
    private final com.bloodbridge.service.SmartDonorMatchingPipelineService pipelineService;

    @Operation(summary = "Execute Smart Donor Matching Pipeline", description = "Triggers the 10-stage Smart Donor Matching Pipeline (Medical Compatibility First, Distance Tier Grouping A-D).")
    @PostMapping("/requests/{requestId}/pipeline")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<com.bloodbridge.dto.response.SmartDonorMatchingPipelineDTO>> executePipeline(@PathVariable Long requestId) {
        log.info("[PIPELINE-REST] Request to execute Smart Donor Matching Pipeline for request ID: {}", requestId);
        ApiResponse<com.bloodbridge.dto.response.SmartDonorMatchingPipelineDTO> response = pipelineService.executePipelineForRequestId(requestId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Pipeline Tier Results", description = "Retrieves Smart Donor Matching Pipeline tier breakdown (Group A, B, C, D) for a request.")
    @GetMapping("/requests/{requestId}/pipeline")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN', 'PATIENT')")
    public ResponseEntity<ApiResponse<com.bloodbridge.dto.response.SmartDonorMatchingPipelineDTO>> getPipelineResults(@PathVariable Long requestId) {
        log.info("[PIPELINE-REST] Request to fetch pipeline tier results for request ID: {}", requestId);
        ApiResponse<com.bloodbridge.dto.response.SmartDonorMatchingPipelineDTO> response = pipelineService.getPipelineResults(requestId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Matching Analytics", description = "Retrieves historical execution analytics for a blood request's matching runs.")
    @GetMapping("/requests/{requestId}/analytics")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<com.bloodbridge.entity.MatchingAnalytics>> getMatchingAnalytics(@PathVariable Long requestId) {
        log.info("[PIPELINE-REST] Request to fetch matching analytics for request ID: {}", requestId);
        ApiResponse<com.bloodbridge.entity.MatchingAnalytics> response = pipelineService.getAnalyticsForRequestId(requestId);
        return ResponseEntity.ok(response);
    }
}
