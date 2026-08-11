package com.bloodbridge.service.impl;

import com.bloodbridge.dto.request.LoginRequest;
import com.bloodbridge.dto.request.RegisterRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.AuthResponse;
import com.bloodbridge.dto.RealtimeEventDTO;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.enums.RealtimeEventType;
import com.bloodbridge.exception.UserAlreadyExistsException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.mapper.UserMapper;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.enums.Role;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.security.JwtService;
import com.bloodbridge.service.AuthService;
import com.bloodbridge.service.NotificationService;
import com.bloodbridge.service.RealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Service implementation for single-role enterprise authentication workflows.
 * Integrates real-time WebSocket broadcasting for registration events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final RealtimeService realtimeService;

    @Override
    @Transactional
    public ApiResponse<String> register(RegisterRequest request) {
        log.info("Processing user registration for email: {} with role: {}", request.getEmail(), request.getRole());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email {} already registered", request.getEmail());
            throw new UserAlreadyExistsException("Email address is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .address(request.getAddress())
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        // Publish WebSocket real-time registration event
        try {
            RealtimeEventType eventType = RealtimeEventType.USER_REGISTERED;
            if (savedUser.getRole() == Role.DONOR) eventType = RealtimeEventType.DONOR_REGISTERED;
            else if (savedUser.getRole() == Role.PATIENT) eventType = RealtimeEventType.PATIENT_REGISTERED;
            else if (savedUser.getRole() == Role.HOSPITAL) eventType = RealtimeEventType.HOSPITAL_REGISTERED;

            RealtimeEventDTO event = RealtimeEventDTO.of(
                    eventType,
                    "USER",
                    savedUser.getId(),
                    "New User Registration",
                    String.format("New %s registered: %s", savedUser.getRole(), savedUser.getFullName()),
                    userMapper.toProfileResponse(savedUser)
            );

            realtimeService.publishAdminUsersUpdate(event);
            realtimeService.publishAdminDashboardUpdate(event);
            if (savedUser.getRole() == Role.HOSPITAL) {
                realtimeService.publishAdminHospitalsUpdate(event);
            }
        } catch (Exception e) {
            log.error("Failed to publish real-time registration event: {}", e.getMessage());
        }

        if (savedUser.getRole() == Role.DONOR && !donorProfileRepository.existsByUserId(savedUser.getId())) {
            DonorProfile profile = DonorProfile.builder()
                    .user(savedUser)
                    .email(savedUser.getEmail())
                    .bloodGroup(request.getBloodGroup())
                    .rhFactor(request.getBloodGroup() != null && request.getBloodGroup().name().contains("POSITIVE") ? "POSITIVE" : "NEGATIVE")
                    .age(request.getAge())
                    .gender(request.getGender())
                    .dateOfBirth(request.getDateOfBirth())
                    .city(request.getCity())
                    .state(request.getState())
                    .weight(request.getWeight())
                    .height(request.getHeight())
                    .country(request.getCountry())
                    .district(request.getDistrict())
                    .postalCode(request.getPostalCode())
                    .address(request.getAddress())
                    .landmark(request.getLandmark())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .alternatePhoneNumber(request.getAlternatePhoneNumber())
                    .aadhaarNumber(request.getAadhaarNumber())
                    .govtIdType(request.getGovtIdType())
                    .govtIdNumber(request.getGovtIdNumber())
                    .occupation(request.getOccupation())
                    .emergencyContactName(request.getEmergencyContactName())
                    .emergencyContactNumber(request.getEmergencyContactNumber())
                    .emergencyContactRelationship(request.getEmergencyContactRelationship())
                    .bmi(request.getBmi())
                    .hemoglobin(request.getHemoglobin())
                    .bloodPressure(request.getBloodPressure())
                    .pulseRate(request.getPulseRate())
                    .medicalConditions(request.getMedicalConditions())
                    .currentMedications(request.getCurrentMedications())
                    .allergies(request.getAllergies())
                    .covidHistory(request.getCovidHistory())
                    .travelHistory(request.getTravelHistory())
                    .smoking(request.getSmoking() != null ? request.getSmoking() : false)
                    .alcohol(request.getAlcohol() != null ? request.getAlcohol() : false)
                    .drugUsage(request.getDrugUsage() != null ? request.getDrugUsage() : false)
                    .pregnancy(request.getPregnancy() != null ? request.getPregnancy() : false)
                    .breastfeeding(request.getBreastfeeding() != null ? request.getBreastfeeding() : false)
                    .recentSurgery(request.getRecentSurgery() != null ? request.getRecentSurgery() : false)
                    .recentTattoo(request.getRecentTattoo() != null ? request.getRecentTattoo() : false)
                    .recentVaccination(request.getRecentVaccination() != null ? request.getRecentVaccination() : false)
                    .recentFever(request.getRecentFever() != null ? request.getRecentFever() : false)
                    .availableForDonation(true)
                    .emergencyAvailable(request.getEmergencyAvailable() != null ? request.getEmergencyAvailable() : true)
                    .preferredDonationRadius(request.getPreferredDonationRadius() != null ? request.getPreferredDonationRadius() : 25.0)
                    .preferredHospitals(request.getPreferredHospitals())
                    .preferredContactMethod(request.getPreferredContactMethod() != null ? request.getPreferredContactMethod() : "EMAIL")
                    .availableDays(request.getAvailableDays())
                    .availableTimeSlots(request.getAvailableTimeSlots())
                    .willingDonatePlatelets(request.getWillingDonatePlatelets() != null ? request.getWillingDonatePlatelets() : true)
                    .willingDonatePlasma(request.getWillingDonatePlasma() != null ? request.getWillingDonatePlasma() : true)
                    .rareBloodDonor(request.getRareBloodDonor() != null ? request.getRareBloodDonor() : false)
                    .pushNotificationEnabled(request.getPushNotificationEnabled() != null ? request.getPushNotificationEnabled() : true)
                    .status("ACTIVE")
                    .verificationStatus("VERIFIED")
                    .totalDonations(0)
                    .livesSaved(0)
                    .donorScore(100)
                    .build();

            donorProfileRepository.save(profile);
            log.info("Initial donor profile created for user ID: {}", savedUser.getId());
            return ApiResponse.success("User registered successfully", "User ID: " + savedUser.getId());
        } else if (savedUser.getRole() == Role.HOSPITAL && !hospitalRepository.existsByUserId(savedUser.getId())) {
            savedUser.setActive(false);
            userRepository.save(savedUser);

            String name = (request.getHospitalName() != null && !request.getHospitalName().isBlank()) ? request.getHospitalName() : savedUser.getFullName();
            String regNum = (request.getRegistrationNumber() != null && !request.getRegistrationNumber().isBlank()) ? request.getRegistrationNumber() : "REG-" + System.currentTimeMillis();

            com.bloodbridge.entity.Hospital hospital = com.bloodbridge.entity.Hospital.builder()
                    .user(savedUser)
                    .hospitalName(name != null ? name : "Registered Hospital")
                    .email(savedUser.getEmail())
                    .phoneNumber(savedUser.getPhoneNumber() != null ? savedUser.getPhoneNumber() : "0000000000")
                    .registrationNumber(regNum)
                    .hospitalType(request.getHospitalType())
                    .website(request.getWebsite())
                    .address(request.getAddress() != null && !request.getAddress().isBlank() ? request.getAddress() : (savedUser.getAddress() != null ? savedUser.getAddress() : "Not Specified"))
                    .city(request.getCity() != null && !request.getCity().isBlank() ? request.getCity() : (savedUser.getCity() != null ? savedUser.getCity() : "City"))
                    .state(request.getState() != null && !request.getState().isBlank() ? request.getState() : (savedUser.getState() != null ? savedUser.getState() : "State"))
                    .country(request.getCountry() != null ? request.getCountry() : savedUser.getCountry())
                    .postalCode(request.getPostalCode() != null ? request.getPostalCode() : savedUser.getPostalCode())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .verified(false)
                    .verificationStatus("PENDING")
                    .build();

            hospitalRepository.save(hospital);
            log.info("Initial hospital profile created for user ID: {} with status PENDING", savedUser.getId());

            try {
                notificationService.notifyAdmin(
                        "New Hospital Registration",
                        String.format("New hospital registered: %s (Pending Verification)", hospital.getHospitalName()),
                        NotificationType.HOSPITAL_REGISTRATION,
                        "/admin/hospitals"
                );
            } catch (Exception e) {
                log.error("Failed to notify admins of hospital registration: {}", e.getMessage());
            }

            return ApiResponse.success("Hospital registration submitted successfully. Pending admin approval.", "User ID: " + savedUser.getId());
        }

        log.info("User registered successfully with ID: {}", savedUser.getId());
        return ApiResponse.success("User registered successfully", "User ID: " + savedUser.getId());
    }

    @Override
    @Transactional
    public ApiResponse<AuthResponse> login(LoginRequest request) {
        log.info("Authenticating user with email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Invalid email or password"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            if (user.getRole() == Role.HOSPITAL) {
                java.util.Optional<com.bloodbridge.entity.Hospital> hospOpt = hospitalRepository.findByUserId(user.getId());
                if (hospOpt.isPresent() && hospOpt.get().isApprovedOrVerified()) {
                    log.info("Hospital account for user {} is approved in database. Synchronizing user active status.", user.getEmail());
                    user.setActive(true);
                    user = userRepository.saveAndFlush(user);
                } else if (hospOpt.isPresent() && "REJECTED".equalsIgnoreCase(hospOpt.get().getVerificationStatus())) {
                    throw new IllegalStateException("Your hospital registration request was rejected by administrator.");
                } else {
                    throw new IllegalStateException("Your hospital account registration is pending admin approval.");
                }
            } else {
                throw new IllegalStateException("Your account is deactivated. Please contact support.");
            }
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtService.generateToken(user);
        AuthResponse authResponse = userMapper.toAuthResponse(user, token);

        log.info("User {} authenticated successfully with role: {}", user.getEmail(), user.getRole());
        return ApiResponse.success("Login successful", authResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AuthResponse.UserInfo> getCurrentUser(String email) {
        log.info("Fetching current user info for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
        AuthResponse.UserInfo userInfo = userMapper.toUserInfo(user);
        return ApiResponse.success("Current user profile retrieved successfully", userInfo);
    }

    @Override
    @Transactional
    public ApiResponse<AuthResponse> switchRole(String email, String targetRole) {
        log.info("Switching role for user email: {} to target role: {}", email, targetRole);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));

        Role newRole;
        try {
            newRole = Role.valueOf(targetRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role specified for switch: " + targetRole);
        }

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);

        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                    auth.getPrincipal(),
                    auth.getCredentials(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + newRole.name()))
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }

        String newToken = jwtService.generateToken(updatedUser);
        AuthResponse authResponse = userMapper.toAuthResponse(updatedUser, newToken);

        log.info("Successfully switched role for user {} to {}", email, newRole);
        return ApiResponse.success("Role switched successfully", authResponse);
    }
}
