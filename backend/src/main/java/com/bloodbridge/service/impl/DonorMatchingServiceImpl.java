package com.bloodbridge.service.impl;

import com.bloodbridge.dto.DonorMatchingResult;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.EmailNotificationRepository;
import com.bloodbridge.service.DonorMatchingService;
import com.bloodbridge.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Production-grade implementation of the Smart Donor Matching Engine.
 * Evaluates 8 strict eligibility criteria including Haversine 50 KM radius filtering,
 * donation cooldown, and duplicate prevention.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DonorMatchingServiceImpl implements DonorMatchingService {

    private static final double MAX_RADIUS_KM = 50.0;
    private static final int COOLDOWN_DAYS = 90;

    private final DonorProfileRepository donorProfileRepository;
    private final EmailNotificationRepository emailNotificationRepository;
    private final LocationService locationService;

    @Override
    public DonorMatchingResult evaluateEligibleDonors(BloodRequest request) {
        if (request == null) {
            log.warn("[DONOR-MATCHING] Received null BloodRequest");
            return DonorMatchingResult.builder().matchedDonors(List.of()).build();
        }

        long startTime = System.currentTimeMillis();
        BloodGroup neededGroup = request.getBloodGroupNeeded();
        Hospital hospital = request.getHospital();

        Double hospitalLat = hospital != null ? hospital.getLatitude() : null;
        Double hospitalLon = hospital != null ? hospital.getLongitude() : null;
        String hospitalCity = hospital != null ? hospital.getCity() : null;
        String hospitalState = hospital != null ? hospital.getState() : null;

        log.info("================================================================================");
        log.info("[DONOR-MATCHING-START] Evaluating Donors for Emergency Request #{}", request.getId());
        log.info(" - Hospital Name   : {}", hospital != null ? hospital.getHospitalName() : "Unknown");
        log.info(" - Hospital Coords : ({}, {})", hospitalLat, hospitalLon);
        log.info(" - Blood Group     : {}", neededGroup);

        List<DonorProfile> allDonors = donorProfileRepository.findAll();
        long totalEvaluated = allDonors.size();
        log.info("[DONOR-MATCHING] Total donors fetched from DB: {}", totalEvaluated);

        List<DonorProfile> matchedDonors = new ArrayList<>();
        Map<Long, Double> donorDistances = new HashMap<>();

        long compatibleCount = 0;
        long withinRadiusCount = 0;

        for (DonorProfile donor : allDonors) {
            User user = donor.getUser();

            // Rule 1: Active User Account
            if (user == null || !Boolean.TRUE.equals(user.getActive()) ||
                    "INACTIVE".equalsIgnoreCase(donor.getStatus()) || "DELETED".equalsIgnoreCase(donor.getStatus())) {
                log.debug("[MATCH-SKIP] Donor ID {}: Inactive user account", donor.getId());
                continue;
            }

            // Rule 2: Email non-null & non-blank
            String donorEmail = (user.getEmail() != null && !user.getEmail().isBlank()) ? user.getEmail() : donor.getEmail();
            if (donorEmail == null || donorEmail.isBlank()) {
                log.debug("[MATCH-SKIP] Donor ID {}: Missing/null email address", donor.getId());
                continue;
            }

            // Rule 3: Email Verified Check
            if (user.getEmailVerified() != null && !Boolean.TRUE.equals(user.getEmailVerified())) {
                log.debug("[MATCH-SKIP] Donor ID {} ({}): Unverified email address", donor.getId(), donorEmail);
                continue;
            }

            // Rule 4: Available for Donation
            Boolean isAvailable = donor.getIsAvailableForDonation();
            Boolean emergencyAvail = donor.getEmergencyAvailable();
            if (!Boolean.TRUE.equals(isAvailable) || !Boolean.TRUE.equals(emergencyAvail)) {
                log.debug("[MATCH-SKIP] Donor ID {} ({}): Not available for donation (isAvailable={}, emergencyAvailable={})",
                        donor.getId(), donorEmail, isAvailable, emergencyAvail);
                continue;
            }

            // Rule 5: Compatible Blood Group
            if (!isBloodGroupCompatible(donor.getBloodGroup(), neededGroup)) {
                log.debug("[MATCH-SKIP] Donor ID {} ({}): Blood group {} incompatible with required {}",
                        donor.getId(), donorEmail, donor.getBloodGroup(), neededGroup);
                continue;
            }
            compatibleCount++;

            // Rule 6: 50 KM Radius Location Matching (Haversine Formula)
            Double donorLat = donor.getLatitude();
            Double donorLon = donor.getLongitude();
            double distanceKm = Double.MAX_VALUE;

            if (hospitalLat != null && hospitalLon != null && donorLat != null && donorLon != null) {
                distanceKm = locationService.calculateDistance(hospitalLat, hospitalLon, donorLat, donorLon);
                if (distanceKm > MAX_RADIUS_KM) {
                    log.debug("[MATCH-SKIP] Donor ID {} ({}): Distance {} KM exceeds 50 KM radius limit",
                            donor.getId(), donorEmail, distanceKm);
                    continue;
                }
            } else {
                // Fallback to city/state match if exact coordinates are unavailable
                if (!isLocationStringCompatible(donor.getCity(), donor.getState(), hospitalCity, hospitalState)) {
                    log.debug("[MATCH-SKIP] Donor ID {} ({}): Coordinates missing and city/state mismatch (Donor: {}, Hospital: {})",
                            donor.getId(), donorEmail, donor.getCity(), hospitalCity);
                    continue;
                }
                distanceKm = 10.0; // Default fallback distance for city match
            }
            withinRadiusCount++;
            donorDistances.put(donor.getId(), distanceKm);

            // Rule 7: Cooldown Period Check (90 days since last donation)
            if (donor.getLastDonationDate() != null && donor.getLastDonationDate().isAfter(LocalDate.now().minusDays(COOLDOWN_DAYS))) {
                log.debug("[MATCH-SKIP] Donor ID {} ({}): Donated within cooldown period on {}",
                        donor.getId(), donorEmail, donor.getLastDonationDate());
                continue;
            }
            if (donor.getNextEligibleDate() != null && donor.getNextEligibleDate().isAfter(LocalDate.now())) {
                log.debug("[MATCH-SKIP] Donor ID {} ({}): Next eligible date {} is in the future",
                        donor.getId(), donorEmail, donor.getNextEligibleDate());
                continue;
            }

            // Rule 8: Duplicate Prevention Check
            if (emailNotificationRepository.existsByEmergencyRequestIdAndDonorId(request.getId(), donor.getId())) {
                log.debug("[MATCH-SKIP] Donor ID {} ({}): Already notified for Emergency Request #{}",
                        donor.getId(), donorEmail, request.getId());
                continue;
            }

            matchedDonors.add(donor);
            log.info("[MATCH-SUCCESS] Matched Donor ID {} | Name: {} | Distance: {} KM | BloodGroup: {} | Email: {}",
                    donor.getId(), user.getFullName(), distanceKm, donor.getBloodGroup(), donorEmail);
        }

        long totalTimeMs = System.currentTimeMillis() - startTime;
        log.info("--------------------------------------------------------------------------------");
        log.info("[DONOR-MATCHING-SUMMARY]");
        log.info(" - Total Donors Scanned  : {}", totalEvaluated);
        log.info(" - Compatible Group      : {}", compatibleCount);
        log.info(" - Within 50 KM Radius   : {}", withinRadiusCount);
        log.info(" - Final Matched Donors  : {}", matchedDonors.size());
        log.info(" - Evaluation Time       : {} ms", totalTimeMs);
        log.info("================================================================================");

        return DonorMatchingResult.builder()
                .matchedDonors(matchedDonors)
                .donorDistances(donorDistances)
                .totalEvaluatedCount(totalEvaluated)
                .compatibleCount(compatibleCount)
                .withinRadiusCount(withinRadiusCount)
                .build();
    }

    @Override
    public boolean isDonorEligible(DonorProfile donor, BloodRequest request) {
        if (donor == null || request == null) return false;
        DonorMatchingResult result = evaluateEligibleDonors(request);
        return result.getMatchedDonors().stream().anyMatch(d -> d.getId().equals(donor.getId()));
    }

    private boolean isBloodGroupCompatible(BloodGroup donorGroup, BloodGroup neededGroup) {
        return com.bloodbridge.util.BloodCompatibilityMatrix.isCompatible(donorGroup, neededGroup);
    }

    private boolean isLocationStringCompatible(String donorCity, String donorState, String hospCity, String hospState) {
        if (hospCity != null && !hospCity.isBlank() && donorCity != null && !donorCity.isBlank()) {
            return donorCity.equalsIgnoreCase(hospCity.trim()) ||
                    hospCity.toLowerCase().contains(donorCity.toLowerCase()) ||
                    donorCity.toLowerCase().contains(hospCity.toLowerCase());
        }
        if (hospState != null && !hospState.isBlank() && donorState != null && !donorState.isBlank()) {
            return donorState.equalsIgnoreCase(hospState.trim());
        }
        return true;
    }
}
