package com.bloodbridge.service.impl;

import com.bloodbridge.dto.request.AvailabilityRequest;
import com.bloodbridge.dto.request.CreateDonorProfileRequest;
import com.bloodbridge.dto.request.EmergencyAvailabilityRequest;
import com.bloodbridge.dto.request.PreferredRadiusRequest;
import com.bloodbridge.dto.request.UpdateDonorProfileRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.DonationHistoryResponse;
import com.bloodbridge.dto.response.DonorDashboardResponse;
import com.bloodbridge.dto.response.DonorProfileResponse;
import com.bloodbridge.dto.response.EligibilityResponse;
import com.bloodbridge.entity.Donation;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.EligibilityStatus;
import com.bloodbridge.exception.EligibilityViolationException;
import com.bloodbridge.exception.InvalidAgeException;
import com.bloodbridge.exception.InvalidWeightException;
import com.bloodbridge.exception.DonorProfileAlreadyExistsException;
import com.bloodbridge.exception.DonorProfileNotFoundException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.mapper.DonorProfileMapper;
import com.bloodbridge.repository.DonationRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.AuditLoggerService;
import com.bloodbridge.service.DonorProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for Donor Management & Smart Donor Portal workflows.
 */
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.MatchResultRepository;
import com.bloodbridge.repository.NotificationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonorProfileServiceImpl implements DonorProfileService {

    private final DonorProfileRepository donorProfileRepository;
    private final UserRepository userRepository;
    private final DonationRepository donationRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final MatchResultRepository matchResultRepository;
    private final NotificationRepository notificationRepository;
    private final DonorProfileMapper donorProfileMapper;
    private final AuditLoggerService auditLoggerService;
    private final com.bloodbridge.config.MatchingConfig matchingConfig;

    @Override
    @Transactional
    public ApiResponse<DonorProfileResponse> createProfile(String email, CreateDonorProfileRequest request) {
        log.info("Creating donor profile for email: {}", email);
        User user = findUserByEmail(email);

        String targetEmail = (request.getEmail() != null && !request.getEmail().isBlank()) ? request.getEmail() : email;

        if (request.getAge() != null && (request.getAge() < 18 || request.getAge() > 65)) {
            log.warn("Donor profile creation failed: Invalid age {}", request.getAge());
            throw new InvalidAgeException("Donor age must be between 18 and 65");
        }

        if (request.getWeight() != null && request.getWeight() < 50.0) {
            log.warn("Donor profile creation failed: Invalid weight {}", request.getWeight());
            throw new InvalidWeightException("Donor weight must be at least 50.0 kg");
        }

        if (request.getLastDonationDate() != null && request.getLastDonationDate().isAfter(LocalDate.now())) {
            log.warn("Donor profile creation failed: Future donation date {}", request.getLastDonationDate());
            throw new EligibilityViolationException("Last donation date cannot be in the future");
        }

        if (donorProfileRepository.existsByUserId(user.getId())) {
            log.warn("Donor profile creation failed: Profile already exists for user ID: {}", user.getId());
            throw new DonorProfileAlreadyExistsException("Donor profile already exists for user: " + email);
        }

        if (donorProfileRepository.existsByEmail(targetEmail)) {
            log.warn("Donor profile creation failed: Email already registered for another donor profile: {}", targetEmail);
            throw new DonorProfileAlreadyExistsException("Email address already registered to another donor: " + targetEmail);
        }

        DonorProfile profile = donorProfileMapper.toEntity(request, user);
        profile.setEmail(targetEmail);
        
        // Calculate initial eligibility & next eligible date
        EligibilityResponse eligibility = computeEligibility(profile);
        profile.setNextEligibleDate(eligibility.getNextEligibleDate());
        profile.setDonorScore(calculateSmartDonorScore(profile));

        DonorProfile savedProfile = donorProfileRepository.save(profile);

        auditLoggerService.logEvent("DONOR_REGISTERED", targetEmail, "Donor profile created with blood group: " + savedProfile.getBloodGroup());
        log.info("Donor profile created successfully with ID: {}", savedProfile.getId());

        DonorProfileResponse response = donorProfileMapper.toResponse(savedProfile, eligibility.getStatus(), eligibility.getNextEligibleDate());
        return ApiResponse.success("Donor profile created successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<DonorProfileResponse> getMyProfile(String email) {
        log.info("Fetching donor profile for email: {}", email);
        DonorProfile profile = findDonorByEmail(email);
        EligibilityResponse eligibility = computeEligibility(profile);
        int cooldownDays = matchingConfig != null ? matchingConfig.getCooldownDays() : 90;
        DonorProfileResponse response = donorProfileMapper.toResponse(
                profile,
                eligibility.getStatus(),
                eligibility.getNextEligibleDate(),
                eligibility.isEligible(),
                eligibility.getDaysUntilEligible(),
                cooldownDays
        );
        return ApiResponse.success("Donor profile retrieved successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<DonorProfileResponse> updateProfile(String email, UpdateDonorProfileRequest request) {
        log.info("Updating donor profile for email: {}", email);
        DonorProfile profile = findDonorByEmail(email);

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(profile.getEmail())) {
                if (donorProfileRepository.existsByEmailAndIdNot(newEmail, profile.getId()) || 
                    (userRepository.existsByEmail(newEmail) && !profile.getUser().getEmail().equalsIgnoreCase(newEmail))) {
                    log.warn("Donor profile update failed: Email {} already exists", newEmail);
                    throw new DonorProfileAlreadyExistsException("Email address is already in use: " + newEmail);
                }
                profile.setEmail(newEmail);
                User user = profile.getUser();
                if (user != null) {
                    user.setEmail(newEmail);
                    userRepository.save(user);
                }
            }
        }

        User user = profile.getUser();
        if (user != null) {
            if (request.getFullName() != null && !request.getFullName().isBlank()) user.setFullName(request.getFullName().trim());
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) user.setPhoneNumber(request.getPhoneNumber().trim());
            if (request.getCity() != null && !request.getCity().isBlank()) user.setCity(request.getCity().trim());
            if (request.getState() != null && !request.getState().isBlank()) user.setState(request.getState().trim());
            userRepository.save(user);
        }

        donorProfileMapper.updateEntityFromRequest(request, profile);

        // Recalculate eligibility & score after update
        EligibilityResponse eligibility = computeEligibility(profile);
        profile.setNextEligibleDate(eligibility.getNextEligibleDate());
        profile.setDonorScore(calculateSmartDonorScore(profile));

        DonorProfile updatedProfile = donorProfileRepository.save(profile);

        auditLoggerService.logEvent("DONOR_PROFILE_UPDATED", updatedProfile.getEmail(), "Donor profile updated");
        log.info("Successfully updated donor profile for ID: {}", updatedProfile.getId());

        DonorProfileResponse response = donorProfileMapper.toResponse(updatedProfile, eligibility.getStatus(), eligibility.getNextEligibleDate());
        return ApiResponse.success("Donor profile updated successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<DonorProfileResponse> toggleAvailability(String email, AvailabilityRequest request) {
        log.info("Toggling donation availability to {} for email: {}", request.getAvailableForDonation(), email);
        DonorProfile profile = findDonorByEmail(email);

        profile.setAvailableForDonation(request.getAvailableForDonation());
        profile.setDonorScore(calculateSmartDonorScore(profile));
        DonorProfile updatedProfile = donorProfileRepository.save(profile);

        auditLoggerService.logEvent("AVAILABILITY_CHANGED", email, "Donation availability set to: " + request.getAvailableForDonation());
        log.info("Successfully updated donation availability for donor ID: {}", updatedProfile.getId());

        EligibilityResponse eligibility = computeEligibility(updatedProfile);
        DonorProfileResponse response = donorProfileMapper.toResponse(updatedProfile, eligibility.getStatus(), eligibility.getNextEligibleDate());
        return ApiResponse.success("Donation availability updated successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<DonorProfileResponse> updateEmergencyAvailability(String email, EmergencyAvailabilityRequest request) {
        log.info("Updating emergency availability to {} for email: {}", request.getEmergencyAvailable(), email);
        DonorProfile profile = findDonorByEmail(email);

        profile.setEmergencyAvailable(request.getEmergencyAvailable());
        profile.setDonorScore(calculateSmartDonorScore(profile));
        DonorProfile updatedProfile = donorProfileRepository.save(profile);

        auditLoggerService.logEvent("EMERGENCY_AVAILABILITY_CHANGED", email, "Emergency availability set to: " + request.getEmergencyAvailable());
        log.info("Successfully updated emergency availability for donor ID: {}", updatedProfile.getId());

        EligibilityResponse eligibility = computeEligibility(updatedProfile);
        DonorProfileResponse response = donorProfileMapper.toResponse(updatedProfile, eligibility.getStatus(), eligibility.getNextEligibleDate());
        return ApiResponse.success("Emergency availability updated successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<DonorProfileResponse> updatePreferredRadius(String email, PreferredRadiusRequest request) {
        log.info("Updating preferred radius to {} KM for email: {}", request.getPreferredDonationRadius(), email);
        DonorProfile profile = findDonorByEmail(email);

        profile.setPreferredDonationRadius(request.getPreferredDonationRadius());
        DonorProfile updatedProfile = donorProfileRepository.save(profile);

        auditLoggerService.logEvent("PREFERRED_RADIUS_UPDATED", email, "Preferred radius set to: " + request.getPreferredDonationRadius() + " KM");
        log.info("Successfully updated preferred radius for donor ID: {}", updatedProfile.getId());

        EligibilityResponse eligibility = computeEligibility(updatedProfile);
        DonorProfileResponse response = donorProfileMapper.toResponse(updatedProfile, eligibility.getStatus(), eligibility.getNextEligibleDate());
        return ApiResponse.success("Preferred donation radius updated successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<DonorDashboardResponse> getDashboard(String email) {
        log.info("Calculating Smart Donor Dashboard metrics for email: {}", email);
        DonorProfile profile = findDonorByEmail(email);

        EligibilityResponse eligibility = computeEligibility(profile);
        int completionPct = calculateProfileCompletionPercentage(profile);
        int donorScore = calculateSmartDonorScore(profile);
        int cooldownDays = matchingConfig != null ? matchingConfig.getCooldownDays() : 90;

        // Dynamic queries
        List<com.bloodbridge.entity.BloodRequest> activeRequests = bloodRequestRepository.findByStatusIn(
                List.of(com.bloodbridge.enums.RequestStatus.CREATED, com.bloodbridge.enums.RequestStatus.MATCHING, com.bloodbridge.enums.RequestStatus.PENDING, com.bloodbridge.enums.RequestStatus.VERIFIED)
        );
        long nearbyCount = activeRequests.stream()
                .filter(r -> profile.getBloodGroup() == null || r.getBloodGroupNeeded() == profile.getBloodGroup() || profile.getBloodGroup() == com.bloodbridge.enums.BloodGroup.O_NEGATIVE)
                .filter(r -> profile.getCity() == null || r.getHospital() == null || r.getHospital().getCity() == null || r.getHospital().getCity().equalsIgnoreCase(profile.getCity()))
                .count();

        long pendingMatches = matchResultRepository.countByDonorIdAndStatus(profile.getId(), com.bloodbridge.enums.MatchStatus.MATCHED) +
                donationRepository.countByDonorIdAndStatusIn(profile.getId(), List.of(com.bloodbridge.enums.DonationStatus.PENDING, com.bloodbridge.enums.DonationStatus.ACCEPTED, com.bloodbridge.enums.DonationStatus.CONFIRMED));

        long unreadNotificationsCount = profile.getUser() != null ? notificationRepository.countUnreadByRecipientUserId(profile.getUser().getId()) : 0;

        DonorDashboardResponse dashboard = DonorDashboardResponse.builder()
                .profileCompletionPercentage(completionPct)
                .bloodGroup(profile.getBloodGroup())
                .eligibilityStatus(eligibility.getStatus())
                .daysUntilEligible(eligibility.getDaysUntilEligible())
                .nextEligibleDate(eligibility.getNextEligibleDate())
                .eligible(eligibility.isEligible())
                .cooldownDays(cooldownDays)
                .totalDonations(profile.getTotalDonations())
                .livesSaved(profile.getLivesSaved())
                .nearbyActiveRequestsCount((int) nearbyCount)
                .pendingDonationRequests((int) pendingMatches)
                .completedDonations(profile.getTotalDonations())
                .recentNotificationsCount((int) unreadNotificationsCount)
                .donorScore(donorScore)
                .fullName(profile.getUser() != null ? profile.getUser().getFullName() : null)
                .city(profile.getCity())
                .state(profile.getState())
                .district(profile.getDistrict())
                .email(profile.getEmail() != null ? profile.getEmail() : (profile.getUser() != null ? profile.getUser().getEmail() : null))
                .phoneNumber(profile.getUser() != null ? profile.getUser().getPhoneNumber() : null)
                .gender(profile.getGender() != null ? profile.getGender().name() : null)
                .age(profile.getAge())
                .weight(profile.getWeight())
                .lastDonationDate(profile.getLastDonationDate())
                .availableForDonation(profile.getAvailableForDonation())
                .emergencyAvailable(profile.getEmergencyAvailable())
                .createdAt(profile.getCreatedAt())
                .healthStatus(eligibility.getStatus() != null ? eligibility.getStatus().name() : "ELIGIBLE")
                .build();

        return ApiResponse.success("Donor dashboard summary retrieved successfully", dashboard);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<EligibilityResponse> calculateEligibility(String email) {
        log.info("Computing smart eligibility report for email: {}", email);
        DonorProfile profile = findDonorByEmail(email);
        EligibilityResponse response = computeEligibility(profile);
        auditLoggerService.logEvent("ELIGIBILITY_CALCULATED", email, "Status: " + response.getStatus());
        return ApiResponse.success("Eligibility report generated successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<DonationHistoryResponse>> getDonationHistory(String email) {
        log.info("Fetching donation history timeline for email: {}", email);
        DonorProfile profile = findDonorByEmail(email);

        List<Donation> donations = donationRepository.findByDonorId(profile.getId());
        List<DonationHistoryResponse> history = new ArrayList<>();

        for (Donation donation : donations) {
            history.add(DonationHistoryResponse.builder()
                    .id(donation.getId())
                    .donationDate(donation.getDonationDate())
                    .hospitalName(donation.getHospital() != null ? donation.getHospital().getHospitalName() : "Community Blood Bank")
                    .unitsDonated(donation.getUnitsDonated())
                    .bloodGroup(profile.getBloodGroup())
                    .status(donation.getStatus())
                    .certificateUrl("https://certificates.bloodbridge.com/cert_" + donation.getId() + ".pdf")
                    .doctorNotes(donation.getRemarks() != null ? donation.getRemarks() : "Standard blood donation verified")
                    .donationType("WHOLE_BLOOD")
                    .createdAt(donation.getCreatedAt())
                    .build());
        }

        return ApiResponse.success("Donation history timeline retrieved successfully", history);
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteProfile(String email) {
        log.info("Soft deleting (deactivating) donor profile for email: {}", email);
        DonorProfile profile = findDonorByEmail(email);

        profile.setStatus("DEACTIVATED");
        profile.setAvailableForDonation(false);
        profile.setEmergencyAvailable(false);
        donorProfileRepository.save(profile);

        auditLoggerService.logEvent("DONOR_PROFILE_DEACTIVATED", email, "Donor profile soft deleted");
        log.info("Successfully deactivated donor profile for ID: {}", profile.getId());

        return ApiResponse.success("Donor profile deactivated successfully");
    }

    // ==========================================
    // HELPER & SMART ENGINE ALGORITHMS
    // ==========================================

    private EligibilityResponse computeEligibility(DonorProfile profile) {
        // 1. Permanent Deferral Checks (Medical conditions)
        if (profile.getMedicalConditions() != null) {
            String cond = profile.getMedicalConditions().toLowerCase();
            if (cond.contains("hiv") || cond.contains("hepatitis") || cond.contains("cancer") || cond.contains("heart disease")) {
                return EligibilityResponse.builder()
                        .status(EligibilityStatus.PERMANENTLY_DEFERRED)
                        .eligible(false)
                        .nextEligibleDate(null)
                        .daysUntilEligible(-1)
                        .reason("Permanent deferral due to recorded chronic medical condition: " + profile.getMedicalConditions())
                        .recommendation("Thank you for your willingness to help. Based on health guidelines, you are permanently exempt from blood donation.")
                        .build();
            }
        }

        // 2. Age & Weight Deferral Checks
        if (profile.getAge() < 18 || profile.getAge() > 65) {
            return EligibilityResponse.builder()
                    .status(EligibilityStatus.TEMPORARILY_DEFERRED)
                    .eligible(false)
                    .nextEligibleDate(null)
                    .daysUntilEligible(-1)
                    .reason("Age must be between 18 and 65 years for safe blood donation.")
                    .recommendation("Donors must meet standard age requirements.")
                    .build();
        }

        if (profile.getWeight() < 50.0) {
            return EligibilityResponse.builder()
                    .status(EligibilityStatus.TEMPORARILY_DEFERRED)
                    .eligible(false)
                    .nextEligibleDate(null)
                    .daysUntilEligible(-1)
                    .reason("Minimum body weight of 50 kg is required for blood donation.")
                    .recommendation("Focus on maintaining healthy nutrition and weight before attempting donation.")
                    .build();
        }

        // 3. Donation Interval Check (Dynamic Cooldown Period)
        int cooldownDays = matchingConfig != null ? matchingConfig.getCooldownDays() : 90;
        LocalDate today = LocalDate.now();

        LocalDate effectiveLastDonation = profile.getLastDonationDate();
        if (donationRepository != null && profile.getId() != null) {
            try {
                var completedDonations = donationRepository.findByDonorId(profile.getId()).stream()
                        .filter(d -> d.getStatus() == com.bloodbridge.enums.DonationStatus.COMPLETED)
                        .sorted(java.util.Comparator.comparing(com.bloodbridge.entity.Donation::getDonationDate, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                        .collect(java.util.stream.Collectors.toList());
                if (!completedDonations.isEmpty() && completedDonations.get(0).getDonationDate() != null) {
                    LocalDate dDate = completedDonations.get(0).getDonationDate();
                    if (effectiveLastDonation == null || dDate.isAfter(effectiveLastDonation)) {
                        effectiveLastDonation = dDate;
                    }
                }
            } catch (Exception e) {
                log.warn("Error fetching completed donations for donor #{}: {}", profile.getId(), e.getMessage());
            }
        }

        if (effectiveLastDonation != null) {
            LocalDate eligibleDate = effectiveLastDonation.plusDays(cooldownDays);
            if (today.isBefore(eligibleDate)) {
                long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, eligibleDate);
                return EligibilityResponse.builder()
                        .status(EligibilityStatus.TEMPORARILY_DEFERRED)
                        .eligible(false)
                        .nextEligibleDate(eligibleDate)
                        .daysUntilEligible(daysRemaining)
                        .reason("Minimum " + cooldownDays + " days interval required between whole blood donations.")
                        .recommendation("You will become eligible to donate on " + eligibleDate + ". Stay hydrated!")
                        .build();
            }
        }

        // Eligible
        return EligibilityResponse.builder()
                .status(EligibilityStatus.ELIGIBLE)
                .eligible(true)
                .nextEligibleDate(today)
                .daysUntilEligible(0)
                .reason("Donor meets all medical age, weight, and donation interval criteria.")
                .recommendation("You are fully eligible to donate blood today! Check for active nearby emergency requests.")
                .build();
    }

    private int calculateSmartDonorScore(DonorProfile profile) {
        int score = 100; // Base score

        // Add 10 points per total donation
        if (profile.getTotalDonations() != null) {
            score += profile.getTotalDonations() * 10;
        }

        // Add 15 points if regular availability is enabled
        if (Boolean.TRUE.equals(profile.getAvailableForDonation())) {
            score += 15;
        }

        // Add 15 points if emergency availability is enabled
        if (Boolean.TRUE.equals(profile.getEmergencyAvailable())) {
            score += 15;
        }

        return score;
    }

    private int calculateProfileCompletionPercentage(DonorProfile profile) {
        int totalFields = 10;
        int completed = 0;

        if (profile.getBloodGroup() != null) completed++;
        if (profile.getAge() != null) completed++;
        if (profile.getGender() != null) completed++;
        if (profile.getCity() != null && !profile.getCity().isBlank()) completed++;
        if (profile.getState() != null && !profile.getState().isBlank()) completed++;
        if (profile.getWeight() != null) completed++;
        if (profile.getHeight() != null) completed++;
        if (profile.getLatitude() != null && profile.getLongitude() != null) completed++;
        if (profile.getMedicalConditions() != null) completed++;
        if (profile.getCurrentMedications() != null) completed++;

        return (completed * 100) / totalFields;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private DonorProfile findDonorByEmail(String email) {
        User user = findUserByEmail(email);
        return donorProfileRepository.findByUserId(user.getId())
                .or(() -> donorProfileRepository.findByEmail(email))
                .orElseThrow(() -> new DonorProfileNotFoundException("Donor profile not found for user: " + email));
    }
}
