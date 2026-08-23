package com.bloodbridge.service.impl;

import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.MatchDashboardResponse;
import com.bloodbridge.dto.response.MatchResponse;
import com.bloodbridge.engine.MatchingEngine;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.MatchResult;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.exception.BloodRequestNotFoundException;
import com.bloodbridge.mapper.MatchingMapper;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.MatchResultRepository;
import com.bloodbridge.service.AuditLoggerService;
import com.bloodbridge.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.bloodbridge.event.DonorMatchedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Intelligent Blood Matching & Allocation Engine workflows.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final BloodRequestRepository bloodRequestRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final MatchResultRepository matchResultRepository;
    private final MatchingEngine matchingEngine;
    private final MatchingMapper matchingMapper;
    private final AuditLoggerService auditLoggerService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ApiResponse<List<MatchResponse>> triggerMatching(Long bloodRequestId) {
        log.info("Triggering Intelligent Matching Engine for blood request ID: {}", bloodRequestId);
        BloodRequest bloodRequest = bloodRequestRepository.findById(bloodRequestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found with ID: " + bloodRequestId));

        // Fetch candidate available donors
        List<DonorProfile> candidateDonors = donorProfileRepository.findByAvailableForDonationTrue();

        // Run Strategy Matching Engine
        List<MatchResult> rankedMatches = matchingEngine.executeMatching(bloodRequest, candidateDonors);

        // Delete existing matches if recalculating
        matchResultRepository.deleteByBloodRequestId(bloodRequestId);

        // Save new match results
        List<MatchResult> savedMatches = matchResultRepository.saveAll(rankedMatches);

        // Transition request status to MATCHED/MATCHING
        bloodRequest.setStatus(RequestStatus.MATCHED);
        bloodRequestRepository.save(bloodRequest);

        auditLoggerService.logEvent("MATCHING_COMPLETED", "SYSTEM", "Generated " + savedMatches.size() + " matches for request ID: " + bloodRequestId);
        log.info("Matching completed successfully for request ID: {}. Top match score: {}", bloodRequestId, savedMatches.isEmpty() ? 0 : savedMatches.get(0).getMatchScore());

        // Publish event for matched donors (triggers in-app and email notifications)
        if (eventPublisher != null) {
            for (MatchResult match : savedMatches) {
                try {
                    eventPublisher.publishEvent(new DonorMatchedEvent(this, match));
                } catch (Exception ex) {
                    log.error("[DONOR-MATCH-EVENT-ERROR] Failed to publish match event for donor #{}: {}",
                            match.getDonor() != null ? match.getDonor().getId() : "N/A", ex.getMessage());
                }
            }
        }

        List<MatchResponse> response = savedMatches.stream()
                .map(matchingMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Matching engine executed successfully. Found " + response.size() + " compatible donors.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<MatchResponse>> getMatchesByRequestId(Long bloodRequestId) {
        log.info("Fetching matches for blood request ID: {}", bloodRequestId);
        List<MatchResult> matches = matchResultRepository.findByBloodRequestIdOrderByRankAsc(bloodRequestId);

        List<MatchResponse> response = matches.stream()
                .map(matchingMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Match results retrieved successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<MatchResponse>> getAllMatches() {
        log.info("Fetching all system match results");
        List<MatchResult> matches = matchResultRepository.findAll();

        List<MatchResponse> response = matches.stream()
                .map(matchingMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("All match results retrieved successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<List<MatchResponse>> recalculateMatches(Long bloodRequestId) {
        log.info("Recalculating matches for blood request ID: {}", bloodRequestId);
        return triggerMatching(bloodRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<MatchDashboardResponse> getMatchDashboard(Long bloodRequestId) {
        log.info("Fetching match dashboard for blood request ID: {}", bloodRequestId);
        BloodRequest bloodRequest = bloodRequestRepository.findById(bloodRequestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found with ID: " + bloodRequestId));

        List<MatchResult> matches = matchResultRepository.findByBloodRequestIdOrderByRankAsc(bloodRequestId);
        List<MatchResponse> matchResponses = matches.stream()
                .map(matchingMapper::toResponse)
                .collect(Collectors.toList());

        MatchDashboardResponse dashboard = MatchDashboardResponse.builder()
                .requestId(bloodRequest.getId())
                .bloodGroupNeeded(bloodRequest.getBloodGroupNeeded())
                .unitsRequired(bloodRequest.getUnitsRequired())
                .urgencyLevel(bloodRequest.getUrgencyLevel())
                .totalMatchesCount(matches.size())
                .topMatches(matchResponses)
                .build();

        return ApiResponse.success("Match dashboard retrieved successfully", dashboard);
    }
}
