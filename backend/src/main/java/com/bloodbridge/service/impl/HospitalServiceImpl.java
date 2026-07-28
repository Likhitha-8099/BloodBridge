package com.bloodbridge.service.impl;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.HospitalRequest;
import com.bloodbridge.dto.HospitalResponse;
import com.bloodbridge.dto.HospitalSummaryResponse;
import com.bloodbridge.dto.HospitalVerificationResponse;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.DuplicateRegistrationNumberException;
import com.bloodbridge.exception.HospitalAlreadyExistsException;
import com.bloodbridge.exception.HospitalNotFoundException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.mapper.HospitalMapper;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing hospital profiles.
 */
@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final HospitalMapper hospitalMapper;

    @Override
    @Transactional
    public HospitalResponse createHospital(HospitalRequest request) {
        User user = getAuthenticatedUser();

        // Enforce that only users with HOSPITAL role can create a hospital profile
        if (user.getRole() != Role.HOSPITAL) {
            throw new IllegalArgumentException("Only users with HOSPITAL role can create a hospital profile");
        }

        // Check if user already has a hospital profile
        if (hospitalRepository.existsByUserId(user.getId())) {
            throw new HospitalAlreadyExistsException("Hospital profile already exists for user: " + user.getEmail());
        }

        // Ensure registration number is unique
        if (hospitalRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateRegistrationNumberException("Registration number is already registered: " + request.getRegistrationNumber());
        }

        Hospital hospital = hospitalMapper.toEntity(request, user);
        Hospital savedHospital = hospitalRepository.save(hospital);

        return hospitalMapper.toResponse(savedHospital);
    }

    @Override
    @Transactional(readOnly = true)
    public HospitalResponse getMyHospital() {
        User user = getAuthenticatedUser();
        Hospital hospital = hospitalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found for user: " + user.getEmail()));

        return hospitalMapper.toResponse(hospital);
    }

    @Override
    @Transactional
    public HospitalResponse updateHospital(HospitalRequest request) {
        User user = getAuthenticatedUser();
        Hospital hospital = hospitalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found for user: " + user.getEmail()));

        // Ensure registration number uniqueness if updated
        if (!hospital.getRegistrationNumber().equals(request.getRegistrationNumber()) &&
                hospitalRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateRegistrationNumberException("Registration number is already registered: " + request.getRegistrationNumber());
        }

        hospital.setHospitalName(request.getHospitalName());
        hospital.setRegistrationNumber(request.getRegistrationNumber());
        hospital.setEmail(request.getEmail());
        hospital.setPhoneNumber(request.getPhoneNumber());
        hospital.setAddress(request.getAddress());
        hospital.setCity(request.getCity());
        hospital.setState(request.getState());

        Hospital updatedHospital = hospitalRepository.save(hospital);

        return hospitalMapper.toResponse(updatedHospital);
    }

    @Override
    @Transactional
    public ApiResponse deleteHospital() {
        User user = getAuthenticatedUser();
        Hospital hospital = hospitalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found for user: " + user.getEmail()));

        hospitalRepository.delete(hospital);

        return ApiResponse.builder()
                .message("Hospital profile deleted successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HospitalResponse getHospitalById(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found for ID: " + id));

        return hospitalMapper.toResponse(hospital);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HospitalSummaryResponse> getAllHospitals() {
        List<Hospital> hospitals = hospitalRepository.findAll();
        return hospitals.stream()
                .map(hospitalMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HospitalSummaryResponse> searchByCity(String city) {
        List<Hospital> hospitals = hospitalRepository.findByCityIgnoreCase(city);
        return hospitals.stream()
                .map(hospitalMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HospitalVerificationResponse verifyHospital(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new HospitalNotFoundException("Hospital profile not found for ID: " + id));

        hospital.setVerified(true);
        hospitalRepository.save(hospital);

        return HospitalVerificationResponse.builder()
                .message("Hospital verified successfully")
                .verified(true)
                .build();
    }

    /**
     * Helper to retrieve currently authenticated user context.
     */
    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for authenticated email: " + email));
    }
}
