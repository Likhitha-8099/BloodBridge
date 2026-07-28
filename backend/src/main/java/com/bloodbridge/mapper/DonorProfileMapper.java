package com.bloodbridge.mapper;

import com.bloodbridge.dto.DonorProfileRequest;
import com.bloodbridge.dto.DonorProfileResponse;
import com.bloodbridge.dto.DonorSearchResponse;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Mapper component for translating between {@link DonorProfile} entities and their corresponding DTOs.
 */
@Component
public class DonorProfileMapper {

    /**
     * Maps a {@link DonorProfileRequest} to a {@link DonorProfile} entity.
     *
     * @param request the profile request payload
     * @param user    the associated user entity
     * @return the unpersisted donor profile entity
     */
    public DonorProfile toEntity(DonorProfileRequest request, User user) {
        if (request == null) {
            return null;
        }

        return DonorProfile.builder()
                .user(user)
                .bloodGroup(request.getBloodGroup())
                .age(request.getAge())
                .gender(request.getGender())
                .city(request.getCity())
                .state(request.getState())
                .lastDonationDate(request.getLastDonationDate())
                .availableForDonation(request.getAvailableForDonation() != null ? request.getAvailableForDonation() : true)
                .medicalConditions(request.getMedicalConditions())
                .weight(request.getWeight())
                .totalDonations(request.getTotalDonations() != null ? request.getTotalDonations() : 0)
                .build();
    }

    /**
     * Maps a {@link DonorProfile} entity to a detailed {@link DonorProfileResponse}.
     *
     * @param profile          the donor profile entity
     * @param eligible         computed eligibility status
     * @param nextEligibleDate computed next eligible donation date
     * @return the mapped profile response DTO
     */
    public DonorProfileResponse toResponse(DonorProfile profile, boolean eligible, LocalDate nextEligibleDate) {
        if (profile == null) {
            return null;
        }

        User user = profile.getUser();

        return DonorProfileResponse.builder()
                .id(profile.getId())
                .fullName(user != null ? user.getFullName() : null)
                .email(user != null ? user.getEmail() : null)
                .phoneNumber(user != null ? user.getPhoneNumber() : null)
                .bloodGroup(profile.getBloodGroup())
                .age(profile.getAge())
                .gender(profile.getGender())
                .city(profile.getCity())
                .state(profile.getState())
                .lastDonationDate(profile.getLastDonationDate())
                .availableForDonation(profile.getAvailableForDonation())
                .medicalConditions(profile.getMedicalConditions())
                .weight(profile.getWeight())
                .totalDonations(profile.getTotalDonations())
                .eligible(eligible)
                .nextEligibleDate(nextEligibleDate)
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    /**
     * Maps a {@link DonorProfile} entity to a streamlined {@link DonorSearchResponse}.
     *
     * @param profile          the donor profile entity
     * @param eligible         computed eligibility status
     * @param nextEligibleDate computed next eligible donation date
     * @return the mapped search response DTO
     */
    public DonorSearchResponse toSearchResponse(DonorProfile profile, boolean eligible, LocalDate nextEligibleDate) {
        if (profile == null) {
            return null;
        }

        User user = profile.getUser();

        return DonorSearchResponse.builder()
                .fullName(user != null ? user.getFullName() : null)
                .email(user != null ? user.getEmail() : null)
                .phoneNumber(user != null ? user.getPhoneNumber() : null)
                .bloodGroup(profile.getBloodGroup())
                .city(profile.getCity())
                .state(profile.getState())
                .availableForDonation(profile.getAvailableForDonation())
                .eligible(eligible)
                .nextEligibleDate(nextEligibleDate)
                .build();
    }
}
