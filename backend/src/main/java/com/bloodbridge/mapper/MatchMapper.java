package com.bloodbridge.mapper;

import com.bloodbridge.dto.DonorMatchResponse;
import com.bloodbridge.dto.MatchResponse;
import com.bloodbridge.dto.MatchSummaryResponse;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.MatchResult;
import com.bloodbridge.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper component for translating match entities to response DTOs.
 */
@Component
public class MatchMapper {

    /**
     * Maps a {@link MatchResult} entity to a detailed {@link MatchResponse}.
     *
     * @param result the match result entity
     * @return the mapped response DTO
     */
    public MatchResponse toResponse(MatchResult result) {
        if (result == null) {
            return null;
        }

        DonorProfile donor = result.getDonor();
        User donorUser = donor != null ? donor.getUser() : null;

        return MatchResponse.builder()
                .id(result.getId())
                .requestId(result.getBloodRequest() != null ? result.getBloodRequest().getId() : null)
                .donorId(donor != null ? donor.getId() : null)
                .donorName(donorUser != null ? donorUser.getFullName() : null)
                .donorBloodGroup(donor != null ? donor.getBloodGroup() : null)
                .donorCity(donor != null ? donor.getCity() : null)
                .compatibilityScore(result.getBloodCompatibilityScore() != null ? result.getBloodCompatibilityScore().intValue() : (result.getMatchScore() != null ? result.getMatchScore().intValue() : 100))
                .matchedAt(result.getMatchedAt())
                .status(result.getStatus())
                .build();
    }

    /**
     * Maps a {@link MatchResult} entity to a simplified {@link MatchSummaryResponse}.
     *
     * @param result the match result entity
     * @return the mapped summary response DTO
     */
    public MatchSummaryResponse toSummaryResponse(MatchResult result) {
        if (result == null) {
            return null;
        }

        DonorProfile donor = result.getDonor();
        User donorUser = donor != null ? donor.getUser() : null;

        return MatchSummaryResponse.builder()
                .id(result.getId())
                .requestId(result.getBloodRequest() != null ? result.getBloodRequest().getId() : null)
                .donorId(donor != null ? donor.getId() : null)
                .donorName(donorUser != null ? donorUser.getFullName() : null)
                .compatibilityScore(result.getBloodCompatibilityScore() != null ? result.getBloodCompatibilityScore().intValue() : (result.getMatchScore() != null ? result.getMatchScore().intValue() : 100))
                .status(result.getStatus())
                .build();
    }

    /**
     * Maps a {@link DonorProfile} and its calculated compatibility score to a {@link DonorMatchResponse}.
     *
     * @param donor the donor profile
     * @param score the calculated compatibility score
     * @return the donor match response DTO
     */
    public DonorMatchResponse toDonorMatchResponse(DonorProfile donor, Integer score) {
        if (donor == null) {
            return null;
        }

        User user = donor.getUser();

        return DonorMatchResponse.builder()
                .donorId(donor.getId())
                .donorName(user != null ? user.getFullName() : null)
                .bloodGroup(donor.getBloodGroup())
                .age(donor.getAge())
                .gender(donor.getGender())
                .city(donor.getCity())
                .state(donor.getState())
                .weight(donor.getWeight())
                .totalDonations(donor.getTotalDonations())
                .compatibilityScore(score)
                .build();
    }
}
