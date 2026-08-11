package com.bloodbridge.service;

import com.bloodbridge.dto.response.DonorEmergencyRequestDTO;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.enums.Role;
import com.bloodbridge.enums.UrgencyLevel;
import com.bloodbridge.repository.*;
import com.bloodbridge.service.impl.SmartDonorMatchingPipelineServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SmartDonorMatchingEndToEndTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorProfileRepository donorProfileRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private MatchedEmergencyDonorRepository matchedEmergencyDonorRepository;

    @Autowired
    private SmartDonorMatchingPipelineServiceImpl pipelineService;

    @Autowired
    private BloodRequestService bloodRequestService;

    private Hospital hospital;
    private User donorUserO;
    private DonorProfile donorProfileO;
    private User donorUserB;
    private DonorProfile donorProfileB;
    private User donorUserA;
    private DonorProfile donorProfileA;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        // Setup Hospital: Apollo Hospitals Bangalore
        User hospitalUser = User.builder()
                .fullName("Apollo Hospitals Bangalore")
                .email("apollo.e2e@hospital.com")
                .phoneNumber("9876543210")
                .password("Password123!")
                .role(Role.HOSPITAL)
                .active(true)
                .emailVerified(true)
                .latitude(12.9720)
                .longitude(77.5950)
                .build();
        hospitalUser = userRepository.save(hospitalUser);

        hospital = Hospital.builder()
                .user(hospitalUser)
                .hospitalName("Apollo Hospitals")
                .email("apollo.e2e@hospital.com")
                .phoneNumber("0801234567")
                .registrationNumber("REG-APOLLO-001")
                .address("Bannerghatta Road")
                .city("Bangalore")
                .state("Karnataka")
                .latitude(12.9720)
                .longitude(77.5950)
                .verified(true)
                .verificationStatus("APPROVED")
                .status("ACTIVE")
                .build();
        hospital = hospitalRepository.save(hospital);

        // Donor 1: O+ (Medically Compatible with O+ recipient)
        donorUserO = userRepository.save(User.builder()
                .fullName("Donor O Positive")
                .email("donor.o.e2e@test.com")
                .phoneNumber("9876543211")
                .password("Password123!")
                .role(Role.DONOR)
                .active(true)
                .emailVerified(true)
                .latitude(12.9716)
                .longitude(77.5946)
                .build());

        donorProfileO = donorProfileRepository.save(DonorProfile.builder()
                .user(donorUserO)
                .email(donorUserO.getEmail())
                .bloodGroup(BloodGroup.O_POSITIVE)
                .age(28)
                .gender(com.bloodbridge.enums.Gender.MALE)
                .city("Bangalore")
                .state("Karnataka")
                .weight(72.0)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .latitude(12.9716)
                .longitude(77.5946)
                .build());

        // Donor 2: B+ (Incompatible with O+ recipient)
        donorUserB = userRepository.save(User.builder()
                .fullName("Donor B Positive")
                .email("donor.b.e2e@test.com")
                .phoneNumber("9876543212")
                .password("Password123!")
                .role(Role.DONOR)
                .active(true)
                .emailVerified(true)
                .latitude(12.9716)
                .longitude(77.5946)
                .build());

        donorProfileB = donorProfileRepository.save(DonorProfile.builder()
                .user(donorUserB)
                .email(donorUserB.getEmail())
                .bloodGroup(BloodGroup.B_POSITIVE)
                .age(30)
                .gender(com.bloodbridge.enums.Gender.MALE)
                .city("Bangalore")
                .state("Karnataka")
                .weight(75.0)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .latitude(12.9716)
                .longitude(77.5946)
                .build());

        // Donor 3: A+ (Incompatible with O+ recipient)
        donorUserA = userRepository.save(User.builder()
                .fullName("Donor A Positive")
                .email("donor.a.e2e@test.com")
                .phoneNumber("9876543213")
                .password("Password123!")
                .role(Role.DONOR)
                .active(true)
                .emailVerified(true)
                .latitude(12.9716)
                .longitude(77.5946)
                .build());

        donorProfileA = donorProfileRepository.save(DonorProfile.builder()
                .user(donorUserA)
                .email(donorUserA.getEmail())
                .bloodGroup(BloodGroup.A_POSITIVE)
                .age(25)
                .gender(com.bloodbridge.enums.Gender.FEMALE)
                .city("Bangalore")
                .state("Karnataka")
                .weight(62.0)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .latitude(12.9716)
                .longitude(77.5946)
                .build());
    }

    @Test
    @DisplayName("End-to-End Test: Emergency Blood Request for O+ recipient matches ONLY O+ donor")
    void testEmergencyBloodRequestMatchingPipeline() {
        // Create emergency blood request for O+
        BloodRequest bloodRequest = BloodRequest.builder()
                .hospital(hospital)
                .bloodGroupNeeded(BloodGroup.O_POSITIVE)
                .unitsRequired(1)
                .urgencyLevel(UrgencyLevel.HIGH)
                .requestDate(LocalDateTime.now())
                .requiredByDate(LocalDate.now().plusDays(1))
                .reason("Emergency Transfusion")
                .status(RequestStatus.CREATED)
                .build();
        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        // Execute matching pipeline
        pipelineService.executePipeline(savedRequest);

        List<MatchedEmergencyDonor> matches = matchedEmergencyDonorRepository.findAll();
        List<MatchedEmergencyDonor> reqMatches = matches.stream()
                .filter(m -> m.getBloodRequest().getId().equals(savedRequest.getId()))
                .toList();

        System.out.println("=== MATCHED DONORS FOR REQUEST #" + savedRequest.getId() + " ===");
        List<Long> matchedDonorIds = reqMatches.stream().map(m -> m.getDonor().getId()).toList();
        for (MatchedEmergencyDonor m : reqMatches) {
            System.out.println("Matched Donor ID: " + m.getDonor().getId() + ", Email: " + m.getDonor().getEmail() + ", BloodGroup: " + m.getDonor().getBloodGroup());
        }

        // Verify O+ donor is matched
        assertThat(matchedDonorIds).contains(donorProfileO.getId());

        // Verify B+ and A+ donors are strictly excluded by compatibility engine
        assertThat(matchedDonorIds).doesNotContain(donorProfileB.getId());
        assertThat(matchedDonorIds).doesNotContain(donorProfileA.getId());

        MatchedEmergencyDonor matchedRecord = reqMatches.stream()
                .filter(m -> m.getDonor().getId().equals(donorProfileO.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(matchedRecord.getMatchingGroup()).isEqualTo("GROUP_A");

        // Authenticate as Donor O+ and test GET /api/v1/donor/emergency-requests service
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(donorUserO.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );
        List<DonorEmergencyRequestDTO> donorOMatches = bloodRequestService.getMatchedEmergencyRequestsForDonor();
        assertThat(donorOMatches).hasSize(1);
        assertThat(donorOMatches.get(0).getRequestId()).isEqualTo(savedRequest.getId());
        assertThat(donorOMatches.get(0).getHospitalName()).isEqualTo("Apollo Hospitals");
        assertThat(donorOMatches.get(0).getBloodGroup()).isEqualTo("O_POSITIVE");

        // Authenticate as Donor B+ and verify no requests returned
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(donorUserB.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );
        List<DonorEmergencyRequestDTO> donorBMatches = bloodRequestService.getMatchedEmergencyRequestsForDonor();
        assertThat(donorBMatches).isEmpty();

        // Authenticate as Donor A+ and verify no requests returned
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(donorUserA.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );
        List<DonorEmergencyRequestDTO> donorAMatches = bloodRequestService.getMatchedEmergencyRequestsForDonor();
        assertThat(donorAMatches).isEmpty();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
