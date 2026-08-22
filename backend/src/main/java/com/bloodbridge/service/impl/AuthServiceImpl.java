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
import java.util.Optional;

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
    private final com.bloodbridge.repository.PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final RealtimeService realtimeService;

    @Override
    @Transactional
    public ApiResponse<String> register(RegisterRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        log.info("Processing user registration for email: {} with role: {}", email, request.getRole());

        Optional<User> existingUserOpt = userRepository.findByEmail(email);

        User userToSave;
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (Boolean.TRUE.equals(existingUser.getActive())) {
                log.warn("Registration failed - email {} already registered and active", email);
                throw new UserAlreadyExistsException("Email address is already registered: " + email);
            }
            log.info("Reactivating previously soft-deleted/deactivated account for email: {}", email);
            existingUser.setFullName(request.getFullName());
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            existingUser.setPhoneNumber(request.getPhoneNumber());
            existingUser.setRole(request.getRole());
            existingUser.setCity(request.getCity());
            existingUser.setState(request.getState());
            existingUser.setCountry(request.getCountry());
            existingUser.setPostalCode(request.getPostalCode());
            existingUser.setAddress(request.getAddress());
            existingUser.setDateOfBirth(request.getDateOfBirth());
            existingUser.setGender(request.getGender() != null ? request.getGender().name() : null);
            existingUser.setLatitude(request.getLatitude());
            existingUser.setLongitude(request.getLongitude());
            existingUser.setActive(request.getRole() != Role.HOSPITAL);
            if (existingUser.getRoles() == null) {
                existingUser.setRoles(new java.util.HashSet<>());
            }
            existingUser.getRoles().clear();
            existingUser.getRoles().add(request.getRole());
            userToSave = existingUser;
        } else {
            userToSave = User.builder()
                    .fullName(request.getFullName())
                    .email(email)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .phoneNumber(request.getPhoneNumber())
                    .role(request.getRole())
                    .city(request.getCity())
                    .state(request.getState())
                    .country(request.getCountry())
                    .postalCode(request.getPostalCode())
                    .address(request.getAddress())
                    .dateOfBirth(request.getDateOfBirth())
                    .gender(request.getGender() != null ? request.getGender().name() : null)
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .active(request.getRole() != Role.HOSPITAL)
                    .build();
            if (userToSave.getRoles() == null) {
                userToSave.setRoles(new java.util.HashSet<>());
            }
            userToSave.getRoles().add(request.getRole());
        }

        User savedUser = userRepository.save(userToSave);

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

        if (savedUser.getRole() == Role.DONOR) {
            DonorProfile profile = donorProfileRepository.findByUserId(savedUser.getId())
                    .or(() -> donorProfileRepository.findByEmail(savedUser.getEmail()))
                    .orElseGet(() -> DonorProfile.builder().build());

            profile.setUser(savedUser);
            profile.setEmail(savedUser.getEmail());
            profile.setBloodGroup(request.getBloodGroup() != null ? request.getBloodGroup() : (profile.getBloodGroup() != null ? profile.getBloodGroup() : com.bloodbridge.enums.BloodGroup.O_POSITIVE));
            profile.setRhFactor(profile.getBloodGroup() != null && profile.getBloodGroup().name().contains("POSITIVE") ? "POSITIVE" : "NEGATIVE");
            profile.setAge(request.getAge() != null ? request.getAge() : (profile.getAge() != null ? profile.getAge() : 25));
            profile.setGender(request.getGender() != null ? request.getGender() : (profile.getGender() != null ? profile.getGender() : com.bloodbridge.enums.Gender.MALE));
            profile.setDateOfBirth(request.getDateOfBirth() != null ? request.getDateOfBirth() : profile.getDateOfBirth());
            profile.setCity(request.getCity() != null ? request.getCity() : (savedUser.getCity() != null ? savedUser.getCity() : "City"));
            profile.setState(request.getState() != null ? request.getState() : (savedUser.getState() != null ? savedUser.getState() : "State"));
            profile.setWeight(request.getWeight() != null ? request.getWeight() : (profile.getWeight() != null ? profile.getWeight() : 60.0));
            profile.setHeight(request.getHeight() != null ? request.getHeight() : profile.getHeight());
            profile.setCountry(request.getCountry() != null ? request.getCountry() : (savedUser.getCountry() != null ? savedUser.getCountry() : "India"));
            profile.setDistrict(request.getDistrict() != null ? request.getDistrict() : profile.getDistrict());
            profile.setPostalCode(request.getPostalCode() != null ? request.getPostalCode() : savedUser.getPostalCode());
            profile.setAddress(request.getAddress() != null ? request.getAddress() : savedUser.getAddress());
            profile.setLandmark(request.getLandmark() != null ? request.getLandmark() : profile.getLandmark());
            profile.setLatitude(request.getLatitude() != null ? request.getLatitude() : savedUser.getLatitude());
            profile.setLongitude(request.getLongitude() != null ? request.getLongitude() : savedUser.getLongitude());
            profile.setAlternatePhoneNumber(request.getAlternatePhoneNumber() != null ? request.getAlternatePhoneNumber() : profile.getAlternatePhoneNumber());
            profile.setAadhaarNumber(request.getAadhaarNumber() != null ? request.getAadhaarNumber() : profile.getAadhaarNumber());
            profile.setGovtIdType(request.getGovtIdType() != null ? request.getGovtIdType() : profile.getGovtIdType());
            profile.setGovtIdNumber(request.getGovtIdNumber() != null ? request.getGovtIdNumber() : profile.getGovtIdNumber());
            profile.setOccupation(request.getOccupation() != null ? request.getOccupation() : profile.getOccupation());
            profile.setEmergencyContactName(request.getEmergencyContactName() != null ? request.getEmergencyContactName() : "Emergency Contact");
            profile.setEmergencyContactNumber(request.getEmergencyContactNumber() != null ? request.getEmergencyContactNumber() : (savedUser.getPhoneNumber() != null ? savedUser.getPhoneNumber() : "0000000000"));
            profile.setEmergencyContactRelationship(request.getEmergencyContactRelationship() != null ? request.getEmergencyContactRelationship() : "Family");
            profile.setBmi(request.getBmi() != null ? request.getBmi() : profile.getBmi());
            profile.setHemoglobin(request.getHemoglobin() != null ? request.getHemoglobin() : profile.getHemoglobin());
            profile.setBloodPressure(request.getBloodPressure() != null ? request.getBloodPressure() : profile.getBloodPressure());
            profile.setPulseRate(request.getPulseRate() != null ? request.getPulseRate() : profile.getPulseRate());
            profile.setMedicalConditions(request.getMedicalConditions() != null ? request.getMedicalConditions() : profile.getMedicalConditions());
            profile.setCurrentMedications(request.getCurrentMedications() != null ? request.getCurrentMedications() : profile.getCurrentMedications());
            profile.setAllergies(request.getAllergies() != null ? request.getAllergies() : profile.getAllergies());
            profile.setCovidHistory(request.getCovidHistory() != null ? request.getCovidHistory() : profile.getCovidHistory());
            profile.setTravelHistory(request.getTravelHistory() != null ? request.getTravelHistory() : profile.getTravelHistory());
            profile.setSmoking(request.getSmoking() != null ? request.getSmoking() : (profile.getSmoking() != null ? profile.getSmoking() : false));
            profile.setAlcohol(request.getAlcohol() != null ? request.getAlcohol() : (profile.getAlcohol() != null ? profile.getAlcohol() : false));
            profile.setDrugUsage(request.getDrugUsage() != null ? request.getDrugUsage() : (profile.getDrugUsage() != null ? profile.getDrugUsage() : false));
            profile.setPregnancy(request.getPregnancy() != null ? request.getPregnancy() : (profile.getPregnancy() != null ? profile.getPregnancy() : false));
            profile.setBreastfeeding(request.getBreastfeeding() != null ? request.getBreastfeeding() : (profile.getBreastfeeding() != null ? profile.getBreastfeeding() : false));
            profile.setRecentSurgery(request.getRecentSurgery() != null ? request.getRecentSurgery() : (profile.getRecentSurgery() != null ? profile.getRecentSurgery() : false));
            profile.setRecentTattoo(request.getRecentTattoo() != null ? request.getRecentTattoo() : (profile.getRecentTattoo() != null ? profile.getRecentTattoo() : false));
            profile.setRecentVaccination(request.getRecentVaccination() != null ? request.getRecentVaccination() : (profile.getRecentVaccination() != null ? profile.getRecentVaccination() : false));
            profile.setRecentFever(request.getRecentFever() != null ? request.getRecentFever() : (profile.getRecentFever() != null ? profile.getRecentFever() : false));
            profile.setAvailableForDonation(true);
            profile.setEmergencyAvailable(request.getEmergencyAvailable() != null ? request.getEmergencyAvailable() : true);
            profile.setPreferredDonationRadius(request.getPreferredDonationRadius() != null ? request.getPreferredDonationRadius() : 25.0);
            profile.setPreferredHospitals(request.getPreferredHospitals() != null ? request.getPreferredHospitals() : profile.getPreferredHospitals());
            profile.setPreferredContactMethod(request.getPreferredContactMethod() != null ? request.getPreferredContactMethod() : "EMAIL");
            profile.setAvailableDays(request.getAvailableDays() != null ? request.getAvailableDays() : profile.getAvailableDays());
            profile.setAvailableTimeSlots(request.getAvailableTimeSlots() != null ? request.getAvailableTimeSlots() : profile.getAvailableTimeSlots());
            profile.setWillingDonatePlatelets(request.getWillingDonatePlatelets() != null ? request.getWillingDonatePlatelets() : true);
            profile.setWillingDonatePlasma(request.getWillingDonatePlasma() != null ? request.getWillingDonatePlasma() : true);
            profile.setRareBloodDonor(request.getRareBloodDonor() != null ? request.getRareBloodDonor() : false);
            profile.setPushNotificationEnabled(request.getPushNotificationEnabled() != null ? request.getPushNotificationEnabled() : true);
            profile.setStatus("ACTIVE");
            profile.setVerificationStatus("VERIFIED");
            if (profile.getTotalDonations() == null) profile.setTotalDonations(0);
            if (profile.getLivesSaved() == null) profile.setLivesSaved(0);
            if (profile.getDonorScore() == null) profile.setDonorScore(100);

            donorProfileRepository.save(profile);
            log.info("Donor profile created/updated for user ID: {}", savedUser.getId());
            return ApiResponse.success("User registered successfully", "User ID: " + savedUser.getId());
        } else if (savedUser.getRole() == Role.HOSPITAL) {
            savedUser.setActive(false);
            userRepository.save(savedUser);

            String name = (request.getHospitalName() != null && !request.getHospitalName().isBlank()) ? request.getHospitalName() : savedUser.getFullName();
            String regNum = (request.getRegistrationNumber() != null && !request.getRegistrationNumber().isBlank()) ? request.getRegistrationNumber() : "REG-" + System.currentTimeMillis();

            com.bloodbridge.entity.Hospital hospital = hospitalRepository.findByUserId(savedUser.getId())
                    .or(() -> hospitalRepository.findByEmail(savedUser.getEmail()))
                    .orElseGet(() -> com.bloodbridge.entity.Hospital.builder().build());

            hospital.setUser(savedUser);
            hospital.setHospitalName(name != null ? name : "Registered Hospital");
            hospital.setEmail(savedUser.getEmail());
            hospital.setPhoneNumber(savedUser.getPhoneNumber() != null ? savedUser.getPhoneNumber() : "0000000000");
            if (hospital.getRegistrationNumber() == null || hospital.getRegistrationNumber().isBlank()) {
                hospital.setRegistrationNumber(regNum);
            }
            hospital.setHospitalType(request.getHospitalType() != null ? request.getHospitalType() : hospital.getHospitalType());
            hospital.setWebsite(request.getWebsite() != null ? request.getWebsite() : hospital.getWebsite());
            hospital.setAddress(request.getAddress() != null && !request.getAddress().isBlank() ? request.getAddress() : (savedUser.getAddress() != null ? savedUser.getAddress() : "Not Specified"));
            hospital.setCity(request.getCity() != null && !request.getCity().isBlank() ? request.getCity() : (savedUser.getCity() != null ? savedUser.getCity() : "City"));
            hospital.setState(request.getState() != null && !request.getState().isBlank() ? request.getState() : (savedUser.getState() != null ? savedUser.getState() : "State"));
            hospital.setCountry(request.getCountry() != null ? request.getCountry() : savedUser.getCountry());
            hospital.setPostalCode(request.getPostalCode() != null ? request.getPostalCode() : savedUser.getPostalCode());
            hospital.setLatitude(request.getLatitude() != null ? request.getLatitude() : savedUser.getLatitude());
            hospital.setLongitude(request.getLongitude() != null ? request.getLongitude() : savedUser.getLongitude());
            hospital.setVerified(false);
            hospital.setVerificationStatus("PENDING");

            hospitalRepository.save(hospital);
            log.info("Hospital profile created/updated for user ID: {} with status PENDING", savedUser.getId());

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
        } else if (savedUser.getRole() == Role.PATIENT) {
            com.bloodbridge.entity.PatientProfile patient = patientProfileRepository.findByUserId(savedUser.getId())
                    .or(() -> patientProfileRepository.findByEmail(savedUser.getEmail()))
                    .orElseGet(() -> com.bloodbridge.entity.PatientProfile.builder().build());

            patient.setUser(savedUser);
            patient.setAge(request.getAge() != null ? request.getAge() : (patient.getAge() != null ? patient.getAge() : 30));
            patient.setGender(request.getGender() != null ? request.getGender() : patient.getGender());
            patient.setBloodGroup(request.getBloodGroup() != null ? request.getBloodGroup() : (patient.getBloodGroup() != null ? patient.getBloodGroup() : com.bloodbridge.enums.BloodGroup.O_POSITIVE));
            patient.setCity(request.getCity() != null ? request.getCity() : (savedUser.getCity() != null ? savedUser.getCity() : "City"));
            patient.setState(request.getState() != null ? request.getState() : (savedUser.getState() != null ? savedUser.getState() : "State"));
            patient.setCountry(request.getCountry() != null ? request.getCountry() : (savedUser.getCountry() != null ? savedUser.getCountry() : "India"));
            patient.setPostalCode(request.getPostalCode() != null ? request.getPostalCode() : savedUser.getPostalCode());
            patient.setAddress(request.getAddress() != null ? request.getAddress() : savedUser.getAddress());
            patient.setEmergencyContactName(request.getEmergencyContactName() != null ? request.getEmergencyContactName() : "Emergency Contact");
            patient.setEmergencyContactNumber(request.getEmergencyContactNumber() != null ? request.getEmergencyContactNumber() : (savedUser.getPhoneNumber() != null ? savedUser.getPhoneNumber() : "0000000000"));
            patient.setStatus("ACTIVE");

            patientProfileRepository.save(patient);
            log.info("Patient profile created/updated for user ID: {}", savedUser.getId());
            return ApiResponse.success("User registered successfully", "User ID: " + savedUser.getId());
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
