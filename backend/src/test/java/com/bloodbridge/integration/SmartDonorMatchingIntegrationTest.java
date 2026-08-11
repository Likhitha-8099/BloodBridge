package com.bloodbridge.integration;

import com.bloodbridge.dto.response.DonorEmergencyRequestDTO;
import com.bloodbridge.dto.response.SmartDonorMatchingPipelineDTO;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.MatchedEmergencyDonor;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Gender;
import com.bloodbridge.enums.MatchedEmergencyDonorStatus;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.enums.Role;
import com.bloodbridge.enums.UrgencyLevel;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.MatchedEmergencyDonorRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.BloodRequestService;
import com.bloodbridge.service.SmartDonorMatchingPipelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SmartDonorMatchingIntegrationTest {

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorProfileRepository donorProfileRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private MatchedEmergencyDonorRepository matchedEmergencyDonorRepository;

    @Autowired
    private SmartDonorMatchingPipelineService smartDonorMatchingPipelineService;

    @Autowired
    private BloodRequestService bloodRequestService;

    private Hospital hospital;
    private User compatibleDonorUser;
    private DonorProfile compatibleDonorProfile;
    private User incompatibleDonorUser;
    private DonorProfile incompatibleDonorProfile;

    @BeforeEach
    void setUp() {
        String ts = String.valueOf(System.currentTimeMillis());

        User hospitalUser = userRepository.save(User.builder()
                .fullName("St. John Medical Center")
                .email("hospital_" + ts + "@example.com")
                .phoneNumber("+919876543210")
                .role(Role.HOSPITAL)
                .password("password")
                .active(true)
                .build());

        hospital = hospitalRepository.save(Hospital.builder()
                .user(hospitalUser)
                .hospitalName("St. John Medical Center")
                .registrationNumber("REG-" + ts)
                .verificationStatus("APPROVED")
                .phoneNumber("+919876543210")
                .email(hospitalUser.getEmail())
                .address("100 Hosur Road")
                .latitude(12.9345)
                .longitude(77.6101)
                .city("Bangalore")
                .state("Karnataka")
                .build());

        // Compatible donor (O- is compatible with O- request)
        compatibleDonorUser = userRepository.save(User.builder()
                .fullName("Alice Compatible")
                .email("compatible_" + ts + "@example.com")
                .phoneNumber("+919876543212")
                .role(Role.DONOR)
                .password("password")
                .active(true)
                .emailVerified(true)
                .build());

        compatibleDonorProfile = donorProfileRepository.save(DonorProfile.builder()
                .user(compatibleDonorUser)
                .email(compatibleDonorUser.getEmail())
                .bloodGroup(BloodGroup.O_NEGATIVE)
                .latitude(12.9350)
                .longitude(77.6105)
                .age(28)
                .gender(Gender.FEMALE)
                .weight(62.0)
                .city("Bangalore")
                .state("Karnataka")
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .build());

        // Incompatible donor (AB+ cannot donate to O- request)
        incompatibleDonorUser = userRepository.save(User.builder()
                .fullName("Bob Incompatible")
                .email("incompatible_" + ts + "@example.com")
                .phoneNumber("+919876543213")
                .role(Role.DONOR)
                .password("password")
                .active(true)
                .emailVerified(true)
                .build());

        incompatibleDonorProfile = donorProfileRepository.save(DonorProfile.builder()
                .user(incompatibleDonorUser)
                .email(incompatibleDonorUser.getEmail())
                .bloodGroup(BloodGroup.AB_POSITIVE)
                .latitude(12.9355)
                .longitude(77.6110)
                .age(30)
                .gender(Gender.MALE)
                .weight(75.0)
                .city("Bangalore")
                .state("Karnataka")
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .build());
    }

    @Test
    void testEndToEndEmergencyBloodRequestToDonorPipeline() {
        // Step 1: Hospital creates emergency blood request for O- blood
        BloodRequest bloodRequest = BloodRequest.builder()
                .hospital(hospital)
                .bloodGroupNeeded(BloodGroup.O_NEGATIVE)
                .unitsRequired(2)
                .urgencyLevel(UrgencyLevel.CRITICAL)
                .requestDate(LocalDateTime.now())
                .requiredByDate(LocalDate.now().plusDays(1))
                .status(RequestStatus.CREATED)
                .reason("Critical Emergency Surgery")
                .createdAt(LocalDateTime.now())
                .build();

        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        // Step 2: Execute Smart Donor Matching Pipeline Service
        SmartDonorMatchingPipelineDTO pipelineResult = smartDonorMatchingPipelineService.executePipeline(savedRequest);
        assertNotNull(pipelineResult);
        assertTrue(pipelineResult.getTotalEligibleDonors() >= 1);

        // Step 3: Verify persistence of MatchedEmergencyDonor records
        List<MatchedEmergencyDonor> matchedEntries = matchedEmergencyDonorRepository.findByBloodRequestId(savedRequest.getId());
        assertFalse(matchedEntries.isEmpty(), "MatchedEmergencyDonor entries must be created for compatible donors");

        boolean containsCompatible = matchedEntries.stream()
                .anyMatch(m -> m.getDonor().getId().equals(compatibleDonorProfile.getId()));
        assertTrue(containsCompatible, "Compatible donor must be stored in MatchedEmergencyDonor");

        boolean containsIncompatible = matchedEntries.stream()
                .anyMatch(m -> m.getDonor().getId().equals(incompatibleDonorProfile.getId()));
        assertFalse(containsIncompatible, "Incompatible donor must NOT be stored in MatchedEmergencyDonor");

        // Step 4: Verify donor dashboard API returns assigned request for compatible donor
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(compatibleDonorUser.getEmail(), null, List.of())
        );

        List<DonorEmergencyRequestDTO> compatibleDonorRequests = bloodRequestService.getMatchedEmergencyRequestsForDonor();
        assertFalse(compatibleDonorRequests.isEmpty(), "Compatible donor dashboard should receive assigned request");
        DonorEmergencyRequestDTO matchedDTO = compatibleDonorRequests.get(0);
        assertEquals(savedRequest.getId(), matchedDTO.getRequestId());
        assertEquals("St. John Medical Center", matchedDTO.getHospitalName());
        assertEquals(BloodGroup.O_NEGATIVE.name(), matchedDTO.getBloodGroup());
        assertNotNull(matchedDTO.getGoogleMapsUrl());

        // Step 5: Verify incompatible donor receives nothing on dashboard API
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(incompatibleDonorUser.getEmail(), null, List.of())
        );

        List<DonorEmergencyRequestDTO> incompatibleDonorRequests = bloodRequestService.getMatchedEmergencyRequestsForDonor();
        assertTrue(incompatibleDonorRequests.isEmpty(), "Incompatible donor dashboard should receive nothing");

        // Step 6: Compatible donor accepts request
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(compatibleDonorUser.getEmail(), null, List.of())
        );

        DonorEmergencyRequestDTO acceptedDTO = bloodRequestService.acceptMatchedEmergencyRequest(savedRequest.getId());
        assertEquals("ACCEPTED", acceptedDTO.getStatus());

        MatchedEmergencyDonor updatedMed = matchedEmergencyDonorRepository
                .findByBloodRequestIdAndDonorId(savedRequest.getId(), compatibleDonorProfile.getId())
                .orElseThrow();
        assertEquals(MatchedEmergencyDonorStatus.ACCEPTED, updatedMed.getStatus());

        // Step 7: Reject flow
        DonorEmergencyRequestDTO rejectedDTO = bloodRequestService.rejectMatchedEmergencyRequest(savedRequest.getId());
        assertEquals("REJECTED", rejectedDTO.getStatus());
    }
}
