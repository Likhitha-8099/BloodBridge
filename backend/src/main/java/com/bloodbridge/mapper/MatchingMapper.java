package com.bloodbridge.mapper;

import com.bloodbridge.dto.response.MatchResponse;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.MatchResult;
import com.bloodbridge.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper component for translating between {@link MatchResult} entities and DTOs.
 */
@Component
public class MatchingMapper {

    /**
     * Maps a {@link MatchResult} entity to a detailed {@link MatchResponse}.
     *
     * @param match match result entity
     * @return mapped MatchResponse DTO
     */
    public MatchResponse toResponse(MatchResult match) {
        if (match == null) {
            return null;
        }

        DonorProfile donor = match.getDonor();
        User donorUser = donor != null ? donor.getUser() : null;

        return MatchResponse.builder()
                .id(match.getId())
                .requestId(match.getBloodRequest() != null ? match.getBloodRequest().getId() : null)
                .donorId(donor != null ? donor.getId() : null)
                .donorName(donorUser != null ? donorUser.getFullName() : "Anonymous Donor")
                .donorPhoneNumber(donorUser != null ? donorUser.getPhoneNumber() : null)
                .bloodGroup(donor != null ? donor.getBloodGroup() : null)
                .matchScore(match.getMatchScore())
                .bloodCompatibilityScore(match.getBloodCompatibilityScore())
                .distanceScore(match.getDistanceScore())
                .availabilityScore(match.getAvailabilityScore())
                .donorScore(match.getDonorScore())
                .eligibilityStatus(match.getEligibilityStatus())
                .distanceKm(match.getDistanceKm())
                .estimatedTravelTimeMinutes(match.getEstimatedTravelTime())
                .rank(match.getRank())
                .status(match.getStatus())
                .matchedAt(match.getMatchedAt())
                .build();
    }
}
