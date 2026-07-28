package com.bloodbridge.service;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.AvailabilityUpdateRequest;
import com.bloodbridge.dto.DonorProfileRequest;
import com.bloodbridge.dto.DonorProfileResponse;
import com.bloodbridge.dto.DonorSearchResponse;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.enums.BloodGroup;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface defining workflows for managing donor profiles and searches.
 */
public interface DonorProfileService {

    /**
     * Creates a new donor profile for the currently authenticated user.
     *
     * @param request the profile details
     * @return the created profile response
     */
    DonorProfileResponse createProfile(DonorProfileRequest request);

    /**
     * Retrieves the profile of the currently authenticated donor.
     *
     * @return the donor's profile details
     */
    DonorProfileResponse getMyProfile();

    /**
     * Updates the profile of the currently authenticated donor.
     *
     * @param request the updated details
     * @return the updated profile response
     */
    DonorProfileResponse updateProfile(DonorProfileRequest request);

    /**
     * Deletes the profile of the currently authenticated donor.
     *
     * @return an {@link ApiResponse} confirming deletion status
     */
    ApiResponse deleteProfile();

    /**
     * Updates the availability status of the currently authenticated donor.
     *
     * @param request the availability payload
     * @return the updated profile response
     */
    DonorProfileResponse updateAvailability(AvailabilityUpdateRequest request);

    /**
     * Searches for donors matching a specific blood group.
     *
     * @param bloodGroup the blood group to search for
     * @return a list of matching search responses
     */
    List<DonorSearchResponse> searchByBloodGroup(BloodGroup bloodGroup);

    /**
     * Searches for donors located in a specific city.
     *
     * @param city the city name
     * @return a list of matching search responses
     */
    List<DonorSearchResponse> searchByCity(String city);

    /**
     * Retrieves all donors who are marked as available for donation.
     *
     * @return a list of available search responses
     */
    List<DonorSearchResponse> getAvailableDonors();

    /**
     * Checks if the donor is clinically eligible to donate based on their profile data.
     *
     * @param profile the donor's profile
     * @return true if the donor is eligible, false otherwise
     */
    boolean isEligibleForDonation(DonorProfile profile);

    /**
     * Calculates the next date on which the donor is eligible to donate.
     *
     * @param profile the donor's profile
     * @return the next eligible donation date
     */
    LocalDate calculateNextEligibleDate(DonorProfile profile);
}
