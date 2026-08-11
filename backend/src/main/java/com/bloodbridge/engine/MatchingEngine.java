package com.bloodbridge.engine;

import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.MatchResult;
import com.bloodbridge.enums.EligibilityStatus;
import com.bloodbridge.enums.MatchStatus;
import com.bloodbridge.strategy.AvailabilityStrategy;
import com.bloodbridge.strategy.BloodCompatibilityStrategy;
import com.bloodbridge.strategy.DistanceStrategy;
import com.bloodbridge.strategy.DonorScoreStrategy;
import com.bloodbridge.strategy.EligibilityStrategy;
import com.bloodbridge.strategy.RankingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Core Intelligent Blood Matching & Allocation Engine orchestrating strategy components.
 */
@Component
@RequiredArgsConstructor
public class MatchingEngine {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchingEngine.class);

    private final BloodCompatibilityStrategy compatibilityStrategy;
    private final EligibilityStrategy eligibilityStrategy;
    private final DistanceStrategy distanceStrategy;
    private final AvailabilityStrategy availabilityStrategy;
    private final DonorScoreStrategy donorScoreStrategy;
    private final RankingStrategy rankingStrategy;

    /**
     * Runs full matching algorithm for a blood request against candidate donor profiles.
     *
     * @param request target blood request
     * @param candidateDonors list of active donor profiles
     * @return ranked list of MatchResult objects
     */
    public List<MatchResult> executeMatching(BloodRequest request, List<DonorProfile> candidateDonors) {
        log.info("Executing Matching Engine for Blood Request ID: {} (Group Needed: {})", request.getId(), request.getBloodGroupNeeded());
        Hospital hospital = request.getHospital();

        Double hospLat = hospital != null ? hospital.getLatitude() : null;
        Double hospLon = hospital != null ? hospital.getLongitude() : null;

        List<MatchResult> candidates = new ArrayList<>();

        for (DonorProfile donor : candidateDonors) {
            // 1. Eligibility Check
            EligibilityStatus status = eligibilityStrategy.evaluateEligibility(donor);
            if (status != EligibilityStatus.ELIGIBLE) {
                continue; // Exclude non-eligible donors from active matching pool
            }

            // 2. Compatibility Check
            double compScore = compatibilityStrategy.calculateCompatibilityScore(donor.getBloodGroup(), request.getBloodGroupNeeded());
            if (compScore <= 0.0) {
                continue; // Exclude incompatible blood groups
            }

            // 3. Distance Calculation
            double distanceKm = distanceStrategy.calculateDistanceKm(hospLat, hospLon, donor.getLatitude(), donor.getLongitude());
            double distScore = distanceStrategy.calculateDistanceScore(distanceKm, donor.getPreferredDonationRadius());
            int travelTimeMin = distanceStrategy.estimateTravelTimeMinutes(distanceKm);

            // 4. Availability Score
            double availScore = availabilityStrategy.calculateAvailabilityScore(donor);

            // 5. Overall Match Score Calculation
            double overallMatchScore = donorScoreStrategy.calculateOverallMatchScore(compScore, distScore, availScore, donor);

            MatchResult match = MatchResult.builder()
                    .bloodRequest(request)
                    .donor(donor)
                    .matchScore(overallMatchScore)
                    .bloodCompatibilityScore(compScore)
                    .distanceScore(distScore)
                    .availabilityScore(availScore)
                    .donorScore(donor.getDonorScore() != null ? donor.getDonorScore().doubleValue() : 100.0)
                    .eligibilityStatus(status.name())
                    .distanceKm(distanceKm)
                    .estimatedTravelTime(travelTimeMin)
                    .status(MatchStatus.MATCHED)
                    .matchedAt(LocalDateTime.now())
                    .build();

            candidates.add(match);
        }

        log.info("Matching Engine evaluated {} compatible candidate donors for request ID: {}", candidates.size(), request.getId());

        // 6. Ranking & Top N selection
        return rankingStrategy.rankAndSelectTopN(candidates, 20);
    }
}
