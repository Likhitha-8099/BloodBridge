package com.bloodbridge.service.impl;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.PatientProfileRequest;
import com.bloodbridge.dto.PatientProfileResponse;
import com.bloodbridge.dto.PatientSummaryResponse;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.InvalidAgeException;
import com.bloodbridge.exception.PatientProfileAlreadyExistsException;
import com.bloodbridge.exception.PatientProfileNotFoundException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.mapper.PatientProfileMapper;
import com.bloodbridge.repository.PatientProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.PatientProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing patient profiles.
 */
@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class PatientProfileServiceImpl implements PatientProfileService {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;
    private final PatientProfileMapper patientProfileMapper;

    @Override
    @Transactional
    public PatientProfileResponse createProfile(PatientProfileRequest request) {
        User user = getAuthenticatedUser();

        // Enforce that only users with PATIENT role can create a patient profile
        if (user.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("Only users with PATIENT role can create a patient profile");
        }

        // Ensure user does not already have a patient profile
        if (patientProfileRepository.existsByUserId(user.getId())) {
            throw new PatientProfileAlreadyExistsException("Patient profile already exists for user: " + user.getEmail());
        }

        // Validate age range
        validateAge(request.getAge());

        PatientProfile profile = patientProfileMapper.toEntity(request, user);
        PatientProfile savedProfile = patientProfileRepository.save(profile);

        return patientProfileMapper.toResponse(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientProfileResponse getMyProfile() {
        User user = getAuthenticatedUser();
        PatientProfile profile = patientProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new PatientProfileNotFoundException("Patient profile not found for user: " + user.getEmail()));

        return patientProfileMapper.toResponse(profile);
    }

    @Override
    @Transactional
    public PatientProfileResponse updateProfile(PatientProfileRequest request) {
        User user = getAuthenticatedUser();
        PatientProfile profile = patientProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new PatientProfileNotFoundException("Patient profile not found for user: " + user.getEmail()));

        // Validate age range
        validateAge(request.getAge());

        profile.setAge(request.getAge());
        profile.setGender(request.getGender());
        profile.setBloodGroup(request.getBloodGroup());
        profile.setAddress(request.getAddress());
        profile.setCity(request.getCity());
        profile.setState(request.getState());
        profile.setEmergencyContactName(request.getEmergencyContactName());
        profile.setEmergencyContactNumber(request.getEmergencyContactNumber());
        profile.setMedicalHistory(request.getMedicalHistory());

        PatientProfile updatedProfile = patientProfileRepository.save(profile);

        return patientProfileMapper.toResponse(updatedProfile);
    }

    @Override
    @Transactional
    public ApiResponse deleteProfile() {
        User user = getAuthenticatedUser();
        PatientProfile profile = patientProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new PatientProfileNotFoundException("Patient profile not found for user: " + user.getEmail()));

        patientProfileRepository.delete(profile);

        return ApiResponse.builder()
                .message("Patient profile deleted successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PatientProfileResponse getPatientById(Long id) {
        PatientProfile profile = patientProfileRepository.findById(id)
                .orElseThrow(() -> new PatientProfileNotFoundException("Patient profile not found for ID: " + id));

        return patientProfileMapper.toResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientSummaryResponse> getAllPatients() {
        List<PatientProfile> profiles = patientProfileRepository.findAll();
        return profiles.stream()
                .map(patientProfileMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper to validate that age is within the [0, 120] range.
     */
    private void validateAge(Integer age) {
        if (age == null || age < 0 || age > 120) {
            throw new InvalidAgeException("Age must be between 0 and 120. Provided: " + age);
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
