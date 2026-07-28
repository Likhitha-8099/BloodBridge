package com.bloodbridge.service.impl;

import com.bloodbridge.dto.CompatibilityResponse;
import com.bloodbridge.dto.DonorMatchResponse;
import com.bloodbridge.dto.MatchResponse;
import com.bloodbridge.dto.MatchingStatisticsResponse;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.*;
import com.bloodbridge.exception.*;
import com.bloodbridge.event.DonorMatchedEvent;
import com.bloodbridge.mapper.MatchMapper;
import com.bloodbridge.repository.*;
import com.bloodbridge.service.CompatibilityService;
import com.bloodbridge.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for the Blood Matching Engine.
 */
@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final CompatibilityService compatibilityService;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final MatchResultRepository matchResultRepository;
    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final HospitalRepository hospitalRepository;
    private final MatchMapper matchMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CompatibilityResponse getCompatibleBloodGroups(BloodGroup bloodGroup) {
        List<BloodGroup> compatibleGroups = compatibilityService.getCompatibleBloodGroups(bloodGroup);
        return CompatibilityResponse.builder()
                .requestedBloodGroup(bloodGroup)
                .compatibleDonors(compatibleGroups)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonorMatchResponse> findEligibleDonors(Long requestId) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + requestId));

        // Enforce verified status
        if (request.getStatus() != RequestStatus.VERIFIED) {
            throw new RequestNotVerifiedException("Only VERIFIED requests can search for matching donors. Current status: " + request.getStatus());
        }

        List<DonorProfile> allDonors = donorProfileRepository.findAll();
        List<DonorProfile> eligibleDonors = allDonors.stream()
                .filter(this::isDonorEligible)
                .filter(donor -> compatibilityService.isCompatible(donor.getBloodGroup(), request.getBloodGroupNeeded()))
                .collect(Collectors.toList());

        return rankDonors(eligibleDonors, request);
    }

    @Override
    @Transactional
    public List<MatchResponse> generateMatches(Long requestId) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + requestId));

        if (request.getStatus() != RequestStatus.VERIFIED) {
            throw new RequestNotVerifiedException("Only VERIFIED requests can generate matches. Current status: " + request.getStatus());
        }

        List<DonorProfile> allDonors = donorProfileRepository.findAll();
        List<DonorProfile> eligibleDonors = allDonors.stream()
                .filter(this::isDonorEligible)
                .filter(donor -> compatibilityService.isCompatible(donor.getBloodGroup(), request.getBloodGroupNeeded()))
                .toList();

        List<MatchResult> newMatches = new ArrayList<>();
        LocalDateTime matchTime = LocalDateTime.now();

        for (DonorProfile donor : eligibleDonors) {
            // Prevent duplicate matches
            if (matchResultRepository.existsByBloodRequestIdAndDonorId(requestId, donor.getId())) {
                continue;
            }

            Integer score = calculateCompatibilityScore(donor, request);

            MatchResult match = MatchResult.builder()
                    .bloodRequest(request)
                    .donor(donor)
                    .compatibilityScore(score)
                    .matchedAt(matchTime)
                    .status(MatchStatus.MATCHED)
                    .build();

            MatchResult savedMatch = matchResultRepository.save(match);
            newMatches.add(savedMatch);

            eventPublisher.publishEvent(new DonorMatchedEvent(this, savedMatch));
        }

        // Update request status to MATCHED if matches were successfully established
        if (!newMatches.isEmpty()) {
            request.setStatus(RequestStatus.MATCHED);
            bloodRequestRepository.save(request);
        }

        return newMatches.stream()
                .map(matchMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DonorMatchResponse> rankDonors(List<DonorProfile> donors, BloodRequest request) {
        if (donors == null || request == null) {
            return Collections.emptyList();
        }

        return donors.stream()
                .map(donor -> matchMapper.toDonorMatchResponse(donor, calculateCompatibilityScore(donor, request)))
                .sorted((a, b) -> b.getCompatibilityScore().compareTo(a.getCompatibilityScore()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchResponse> getMatchesForRequest(Long requestId) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new BloodRequestNotFoundException("Blood request not found for ID: " + requestId));

        User user = getAuthenticatedUser();

        // Security check: PATIENT owns request, HOSPITAL is assigned to request
        if (user.getRole() == Role.PATIENT) {
            PatientProfile patient = patientProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new PatientProfileNotFoundException("Patient profile not found"));
            if (!request.getPatient().getId().equals(patient.getId())) {
                throw new AccessDeniedException("You are not authorized to view matches for this request");
            }
        } else if (user.getRole() == Role.HOSPITAL) {
            Hospital hospital = hospitalRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found"));
            if (!request.getHospital().getId().equals(hospital.getId())) {
                throw new AccessDeniedException("You are not authorized to view matches for this request");
            }
        }

        List<MatchResult> matches = matchResultRepository.findByBloodRequestId(requestId);
        return matches.stream()
                .map(matchMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchResponse> getMatchesForDonor(Long donorId) {
        User user = getAuthenticatedUser();

        // Security check: DONOR must view their own matches
        if (user.getRole() == Role.DONOR) {
            DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found"));
            if (!donor.getId().equals(donorId)) {
                throw new AccessDeniedException("You are not authorized to view matches for this donor ID");
            }
        }

        List<MatchResult> matches = matchResultRepository.findByDonorId(donorId);
        return matches.stream()
                .map(matchMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Integer calculateCompatibilityScore(DonorProfile donor, BloodRequest request) {
        if (donor == null || request == null) {
            return 0;
        }

        int score = 0;

        // 1. Same City: +50 points
        if (donor.getCity() != null && request.getHospital().getCity() != null &&
                donor.getCity().equalsIgnoreCase(request.getHospital().getCity())) {
            score += 50;
        }

        // 2. Available for donation: +30 points
        if (Boolean.TRUE.equals(donor.getAvailableForDonation())) {
            score += 30;
        }

        // 3. Low Donation Count (< 5): +10 points
        int totalDonations = donor.getTotalDonations() != null ? donor.getTotalDonations() : 0;
        if (totalDonations < 5) {
            score += 10;
        }

        // 4. Recently Updated Profile (within last 30 days): +10 points
        if (donor.getUpdatedAt() != null) {
            long daysSinceUpdate = ChronoUnit.DAYS.between(donor.getUpdatedAt(), LocalDateTime.now());
            if (daysSinceUpdate <= 30) {
                score += 10;
            }
        }

        return score;
    }

    @Override
    @Transactional(readOnly = true)
    public MatchingStatisticsResponse getMatchingStatistics() {
        long totalMatches = matchResultRepository.count();
        long acceptedMatches = matchResultRepository.countByStatus(MatchStatus.ACCEPTED);
        long rejectedMatches = matchResultRepository.countByStatus(MatchStatus.REJECTED);
        long activeMatches = matchResultRepository.countByStatus(MatchStatus.MATCHED);

        return MatchingStatisticsResponse.builder()
                .totalMatches(totalMatches)
                .acceptedMatches(acceptedMatches)
                .rejectedMatches(rejectedMatches)
                .activeMatches(activeMatches)
                .build();
    }

    /**
     * Checks if a donor profile satisfies hard constraints for eligibility.
     */
    private boolean isDonorEligible(DonorProfile donor) {
        if (donor == null || !Boolean.TRUE.equals(donor.getAvailableForDonation())) {
            return false;
        }

        if (donor.getAge() == null || donor.getAge() < 18 || donor.getAge() > 65) {
            return false;
        }

        if (donor.getWeight() == null || donor.getWeight() < 50.0) {
            return false;
        }

        // Verify waiting period elapsed
        if (donor.getLastDonationDate() != null) {
            long daysSinceDonation = ChronoUnit.DAYS.between(donor.getLastDonationDate(), java.time.LocalDate.now());
            int waitInterval = (donor.getGender() == Gender.FEMALE) ? 120 : 90;
            if (daysSinceDonation < waitInterval) {
                return false;
            }
        }

        return true;
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
    }
}
