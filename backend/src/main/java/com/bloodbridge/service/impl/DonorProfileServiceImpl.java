package com.bloodbridge.service.impl;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.AvailabilityUpdateRequest;
import com.bloodbridge.dto.DonorProfileRequest;
import com.bloodbridge.dto.DonorProfileResponse;
import com.bloodbridge.dto.DonorSearchResponse;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Gender;
import com.bloodbridge.exception.DonorProfileAlreadyExistsException;
import com.bloodbridge.exception.DonorProfileNotFoundException;
import com.bloodbridge.exception.EligibilityViolationException;
import com.bloodbridge.exception.InvalidAgeException;
import com.bloodbridge.exception.InvalidWeightException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.mapper.DonorProfileMapper;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.DonorProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing donor profiles and eligibility calculations.
 */
@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class DonorProfileServiceImpl implements DonorProfileService {

    private final DonorProfileRepository donorProfileRepository;
    private final UserRepository userRepository;
    private final DonorProfileMapper donorProfileMapper;

    @Override
    @Transactional
    public DonorProfileResponse createProfile(DonorProfileRequest request) {
        User user = getAuthenticatedUser();

        // Ensure user does not already have a donor profile
        donorProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            throw new DonorProfileAlreadyExistsException("Donor profile already exists for user: " + user.getEmail());
        });

        // Validate basic rules
        validateEligibilityConstraints(request.getAge(), request.getWeight(), request.getLastDonationDate());

        DonorProfile profile = donorProfileMapper.toEntity(request, user);
        DonorProfile savedProfile = donorProfileRepository.save(profile);

        boolean eligible = isEligibleForDonation(savedProfile);
        LocalDate nextEligibleDate = calculateNextEligibleDate(savedProfile);

        return donorProfileMapper.toResponse(savedProfile, eligible, nextEligibleDate);
    }

    @Override
    @Transactional(readOnly = true)
    public DonorProfileResponse getMyProfile() {
        User user = getAuthenticatedUser();
        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found for user: " + user.getEmail()));

        boolean eligible = isEligibleForDonation(profile);
        LocalDate nextEligibleDate = calculateNextEligibleDate(profile);

        return donorProfileMapper.toResponse(profile, eligible, nextEligibleDate);
    }

    @Override
    @Transactional
    public DonorProfileResponse updateProfile(DonorProfileRequest request) {
        User user = getAuthenticatedUser();
        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found for user: " + user.getEmail()));

        // Validate basic rules
        validateEligibilityConstraints(request.getAge(), request.getWeight(), request.getLastDonationDate());

        // Update fields
        profile.setBloodGroup(request.getBloodGroup());
        profile.setAge(request.getAge());
        profile.setGender(request.getGender());
        profile.setCity(request.getCity());
        profile.setState(request.getState());
        profile.setLastDonationDate(request.getLastDonationDate());
        profile.setAvailableForDonation(request.getAvailableForDonation());
        profile.setMedicalConditions(request.getMedicalConditions());
        profile.setWeight(request.getWeight());
        profile.setTotalDonations(request.getTotalDonations());

        DonorProfile updatedProfile = donorProfileRepository.save(profile);

        boolean eligible = isEligibleForDonation(updatedProfile);
        LocalDate nextEligibleDate = calculateNextEligibleDate(updatedProfile);

        return donorProfileMapper.toResponse(updatedProfile, eligible, nextEligibleDate);
    }

    @Override
    @Transactional
    public ApiResponse deleteProfile() {
        User user = getAuthenticatedUser();
        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found for user: " + user.getEmail()));

        donorProfileRepository.delete(profile);

        return ApiResponse.builder()
                .message("Donor profile deleted successfully")
                .build();
    }

    @Override
    @Transactional
    public DonorProfileResponse updateAvailability(AvailabilityUpdateRequest request) {
        User user = getAuthenticatedUser();
        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found for user: " + user.getEmail()));

        profile.setAvailableForDonation(request.getAvailableForDonation());
        DonorProfile updatedProfile = donorProfileRepository.save(profile);

        boolean eligible = isEligibleForDonation(updatedProfile);
        LocalDate nextEligibleDate = calculateNextEligibleDate(updatedProfile);

        return donorProfileMapper.toResponse(updatedProfile, eligible, nextEligibleDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonorSearchResponse> searchByBloodGroup(BloodGroup bloodGroup) {
        List<DonorProfile> profiles = donorProfileRepository.findByBloodGroup(bloodGroup);
        return mapToSearchResponses(profiles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonorSearchResponse> searchByCity(String city) {
        List<DonorProfile> profiles = donorProfileRepository.findByCityIgnoreCase(city);
        return mapToSearchResponses(profiles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonorSearchResponse> getAvailableDonors() {
        List<DonorProfile> profiles = donorProfileRepository.findByAvailableForDonationTrue();
        return mapToSearchResponses(profiles);
    }

    @Override
    public boolean isEligibleForDonation(DonorProfile profile) {
        if (profile.getAge() < 18 || profile.getAge() > 60) {
            return false;
        }
        if (profile.getWeight() < 50.0) {
            return false;
        }
        if (profile.getLastDonationDate() == null) {
            return true;
        }

        long daysSinceLastDonation = ChronoUnit.DAYS.between(profile.getLastDonationDate(), LocalDate.now());
        int requiredDays = getRequiredDaysBetweenDonations(profile.getGender());

        return daysSinceLastDonation >= requiredDays;
    }

    @Override
    public LocalDate calculateNextEligibleDate(DonorProfile profile) {
        if (profile.getLastDonationDate() == null) {
            return LocalDate.now();
        }
        int requiredDays = getRequiredDaysBetweenDonations(profile.getGender());
        return profile.getLastDonationDate().plusDays(requiredDays);
    }

    /**
     * Maps a list of DonorProfiles to search responses.
     */
    private List<DonorSearchResponse> mapToSearchResponses(List<DonorProfile> profiles) {
        return profiles.stream()
                .map(profile -> {
                    boolean eligible = isEligibleForDonation(profile);
                    LocalDate nextEligibleDate = calculateNextEligibleDate(profile);
                    return donorProfileMapper.toSearchResponse(profile, eligible, nextEligibleDate);
                })
                .collect(Collectors.toList());
    }

    /**
     * Resolves the clinical wait time based on gender.
     */
    private int getRequiredDaysBetweenDonations(Gender gender) {
        if (gender == Gender.FEMALE) {
            return 120;
        }
        // MALE and OTHER default to 90 days wait time
        return 90;
    }

    /**
     * Validates basic age and weight eligibility constraints.
     */
    private void validateEligibilityConstraints(Integer age, Double weight, LocalDate lastDonationDate) {
        if (age < 18 || age > 60) {
            throw new InvalidAgeException("Age must be between 18 and 60. Provided: " + age);
        }
        if (weight < 50.0) {
            throw new InvalidWeightException("Weight must be at least 50 kg. Provided: " + weight);
        }
        if (lastDonationDate != null && lastDonationDate.isAfter(LocalDate.now())) {
            throw new EligibilityViolationException("Last donation date cannot be in the future");
        }
    }

    /**
     * Helper to fetch currently logged-in user context.
     */
    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for authenticated email: " + email));
    }
}
