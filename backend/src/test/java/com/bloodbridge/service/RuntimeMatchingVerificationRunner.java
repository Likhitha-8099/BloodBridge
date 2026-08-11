package com.bloodbridge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.request.HospitalBloodRequestCreate;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.response.DonorEmergencyRequestDTO;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Role;
import com.bloodbridge.enums.UrgencyLevel;
import com.bloodbridge.repository.*;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RuntimeMatchingVerificationRunner {

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
    private HospitalService hospitalService;

    @Autowired
    private BloodRequestService bloodRequestService;

    private User hospitalUser;
    private Hospital hospital;
    private User donorUserB;
    private DonorProfile donorProfileB;
    private User donorUserO;
    private DonorProfile donorProfileO;
    private User donorUserA;
    private DonorProfile donorProfileA;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        // 1. Hospital Account: Apollo Hospitals Bangalore (Approved & Verified)
        hospitalUser = userRepository.save(User.builder()
                .fullName("Apollo Hospitals Bangalore")
                .email("apollo.runtime@hospital.com")
                .password("Password123!")
                .phoneNumber("0801234567")
                .role(Role.HOSPITAL)
                .active(true)
                .emailVerified(true)
                .latitude(12.9720)
                .longitude(77.5950)
                .build());

        hospital = hospitalRepository.save(Hospital.builder()
                .user(hospitalUser)
                .hospitalName("Apollo Hospitals")
                .email("apollo.runtime@hospital.com")
                .phoneNumber("0801234567")
                .registrationNumber("REG-APOLLO-RUN")
                .address("Bannerghatta Road")
                .city("Bangalore")
                .state("Karnataka")
                .latitude(12.9720)
                .longitude(77.5950)
                .verified(true)
                .verificationStatus("APPROVED")
                .status("ACTIVE")
                .build());

        // 2. Donor 1: B+ (Medically Compatible with B+ Recipient)
        donorUserB = userRepository.save(User.builder()
                .fullName("Test Donor B Positive")
                .email("donor.b.runtime@test.com")
                .password("Password123!")
                .phoneNumber("9876543201")
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
                .age(29)
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

        // 3. Donor 2: O+ (Medically Compatible with B+ Recipient)
        donorUserO = userRepository.save(User.builder()
                .fullName("Test Donor O Positive")
                .email("donor.o.runtime@test.com")
                .password("Password123!")
                .phoneNumber("9876543202")
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
                .weight(70.0)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .latitude(12.9716)
                .longitude(77.5946)
                .build());

        // 4. Donor 3: A+ (Medically INCOMPATIBLE with B+ Recipient)
        donorUserA = userRepository.save(User.builder()
                .fullName("Test Donor A Positive")
                .email("donor.a.runtime@test.com")
                .password("Password123!")
                .phoneNumber("9876543203")
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
                .age(26)
                .gender(com.bloodbridge.enums.Gender.FEMALE)
                .city("Bangalore")
                .state("Karnataka")
                .weight(64.0)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .latitude(12.9716)
                .longitude(77.5946)
                .build());
    }

    @Test
    @DisplayName("Runtime End-to-End Verification: B+ Emergency Request Creation -> Smart Donor Matching Pipeline -> MatchedEmergencyDonor Persistence -> Donor REST API DTO")
    void executeRuntimeVerificationTrace() throws Exception {
        System.out.println("================================================================================");
        System.out.println("🚀 RUNTIME TRACE STEP 1: Hospital Creates Emergency B+ Blood Request");
        System.out.println("================================================================================");

        HospitalBloodRequestCreate createDto = new HospitalBloodRequestCreate();
        createDto.setBloodGroupNeeded(BloodGroup.B_POSITIVE);
        createDto.setUnitsRequired(1);
        createDto.setUrgencyLevel(UrgencyLevel.HIGH);
        createDto.setRequiredByDate(LocalDate.now().plusDays(1));
        createDto.setReason("Emergency Transfusion Required");

        ApiResponse<BloodRequestResponse> createResponse = hospitalService.createBloodRequest(hospitalUser.getEmail(), createDto);
        BloodRequestResponse savedReqResp = createResponse.getData();
        Long bloodRequestId = savedReqResp.getId();

        System.out.println("✅ Hospital Request Created Successfully! Request ID: #" + bloodRequestId);
        System.out.println(" - Blood Group Needed : " + savedReqResp.getBloodGroupNeeded());
        System.out.println(" - Units Required     : " + savedReqResp.getUnitsRequired());
        System.out.println(" - Urgency Level       : " + savedReqResp.getUrgencyLevel());
        System.out.println(" - Status              : " + savedReqResp.getStatus());

        // Verify blood_requests database row
        BloodRequest reqEntity = bloodRequestRepository.findById(bloodRequestId).orElseThrow();
        assertThat(reqEntity.getBloodGroupNeeded()).isEqualTo(BloodGroup.B_POSITIVE);
        assertThat(reqEntity.getHospital().getId()).isEqualTo(hospital.getId());

        System.out.println("\n================================================================================");
        System.out.println("📊 RUNTIME TRACE STEP 2: Database Check - matched_emergency_donors");
        System.out.println("================================================================================");

        List<MatchedEmergencyDonor> matchedRecords = matchedEmergencyDonorRepository.findAll().stream()
                .filter(m -> m.getBloodRequest().getId().equals(bloodRequestId))
                .toList();

        System.out.println("Found " + matchedRecords.size() + " MatchedEmergencyDonor rows for Blood Request #" + bloodRequestId + ":");
        for (MatchedEmergencyDonor med : matchedRecords) {
            System.out.println(String.format(" -> ID: %d | BloodReq: %d | DonorId: %d (%s) | Group: %s | Distance: %.2f km | Status: %s",
                    med.getId(), med.getBloodRequest().getId(), med.getDonor().getId(), med.getDonor().getBloodGroup(), med.getMatchingGroup(), med.getDistanceKm(), med.getStatus()));
        }

        List<Long> matchedDonorIds = matchedRecords.stream().map(m -> m.getDonor().getId()).toList();
        
        // Assertions for Medical Blood Compatibility (B+ Recipient receives from B+ & O+, strictly excludes A+)
        assertThat(matchedDonorIds).contains(donorProfileB.getId());
        assertThat(matchedDonorIds).contains(donorProfileO.getId());
        assertThat(matchedDonorIds).doesNotContain(donorProfileA.getId());

        System.out.println("\n================================================================================");
        System.out.println("🔑 RUNTIME TRACE STEP 3: Authenticated Donor B+ Calls GET /api/v1/donor/emergency-requests");
        System.out.println("================================================================================");

        // Authenticate as Donor B+
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(donorUserB.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );

        List<DonorEmergencyRequestDTO> donorBApiResult = bloodRequestService.getMatchedEmergencyRequestsForDonor();
        System.out.println("API Output for Donor B+ (" + donorUserB.getEmail() + "):");
        System.out.println(objectMapper.writeValueAsString(donorBApiResult));

        assertThat(donorBApiResult).isNotEmpty();
        assertThat(donorBApiResult.get(0).getRequestId()).isEqualTo(bloodRequestId);
        assertThat(donorBApiResult.get(0).getHospitalName()).isEqualTo("Apollo Hospitals");
        assertThat(donorBApiResult.get(0).getBloodGroup()).isEqualTo("B_POSITIVE");
        assertThat(donorBApiResult.get(0).getPriority()).isEqualTo("HIGH");

        System.out.println("\n================================================================================");
        System.out.println("🔑 RUNTIME TRACE STEP 4: Authenticated Donor A+ Calls GET /api/v1/donor/emergency-requests");
        System.out.println("================================================================================");

        // Authenticate as Donor A+
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(donorUserA.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );

        List<DonorEmergencyRequestDTO> donorAApiResult = bloodRequestService.getMatchedEmergencyRequestsForDonor();
        System.out.println("API Output for Donor A+ (" + donorUserA.getEmail() + "): " + donorAApiResult.size() + " items (Empty as expected!)");
        assertThat(donorAApiResult).isEmpty();

        System.out.println("\n================================================================================");
        System.out.println("✅ RUNTIME TRACE STEP 5: Donor B+ Accepts Emergency Blood Request");
        System.out.println("================================================================================");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(donorUserB.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );

        DonorEmergencyRequestDTO acceptedDto = bloodRequestService.acceptMatchedEmergencyRequest(bloodRequestId);
        System.out.println("Acceptance Result DTO:");
        System.out.println(objectMapper.writeValueAsString(acceptedDto));

        assertThat(acceptedDto.getStatus()).isEqualTo("ACCEPTED");

        BloodRequest updatedReq = bloodRequestRepository.findById(bloodRequestId).orElseThrow();
        assertThat(updatedReq.getStatus()).isEqualTo(com.bloodbridge.enums.RequestStatus.DONOR_ACCEPTED);

        System.out.println("================================================================================");
        System.out.println("🎉 RUNTIME VERIFICATION COMPLETE: ALL CHECKS PASSED!");
        System.out.println("================================================================================");
    }
}
