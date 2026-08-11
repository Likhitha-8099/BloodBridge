package com.bloodbridge.service;

import com.bloodbridge.dto.request.HospitalBloodRequestCreate;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.response.DonorEmergencyRequestDTO;
import com.bloodbridge.dto.response.HospitalDonorResponseDTO;
import com.bloodbridge.dto.response.HospitalEmergencyResponsesContainerDTO;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.MatchedEmergencyDonor;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.enums.Role;
import com.bloodbridge.enums.UrgencyLevel;
import com.bloodbridge.exception.InvalidRequestStateException;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.MatchedEmergencyDonorRepository;
import com.bloodbridge.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EnterpriseEmergencyLifecycleIntegrationTest {

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

    private User hospitalUser1;
    private User hospitalUser2;

    private User donorUserB;
    private DonorProfile donorB;
    private User donorUserO;
    private DonorProfile donorO;
    private User donorUserA;
    private DonorProfile donorA;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        // Hospital 1
        hospitalUser1 = userRepository.save(User.builder()
                .fullName("City Hospital Center")
                .email("city.hosp@test.com")
                .password("Password123!")
                .phoneNumber("08099887766")
                .role(Role.HOSPITAL)
                .active(true)
                .emailVerified(true)
                .latitude(12.9716)
                .longitude(77.5946)
                .build());

        hospitalRepository.save(Hospital.builder()
                .user(hospitalUser1)
                .hospitalName("City Hospital Center")
                .email(hospitalUser1.getEmail())
                .phoneNumber("08099887766")
                .registrationNumber("REG-CITY-01")
                .address("123 Health Ave, Indiranagar")
                .city("Bangalore")
                .state("Karnataka")
                .latitude(12.9716)
                .longitude(77.5946)
                .verified(true)
                .verificationStatus("APPROVED")
                .status("ACTIVE")
                .build());

        // Hospital 2 (Unauthorized hospital)
        hospitalUser2 = userRepository.save(User.builder()
                .fullName("Metro General Hospital")
                .email("metro.hosp@test.com")
                .password("Password123!")
                .phoneNumber("08011223344")
                .role(Role.HOSPITAL)
                .active(true)
                .emailVerified(true)
                .latitude(12.9352)
                .longitude(77.6245)
                .build());

        hospitalRepository.save(Hospital.builder()
                .user(hospitalUser2)
                .hospitalName("Metro General Hospital")
                .email(hospitalUser2.getEmail())
                .phoneNumber("08011223344")
                .registrationNumber("REG-METRO-02")
                .address("456 Care Blvd, Koramangala")
                .city("Bangalore")
                .state("Karnataka")
                .latitude(12.9352)
                .longitude(77.6245)
                .verified(true)
                .verificationStatus("APPROVED")
                .status("ACTIVE")
                .build());

        // Donor 1: B+ (Compatible for B+)
        donorUserB = userRepository.save(User.builder()
                .fullName("Rohan Verma")
                .email("rohan.b@donor.com")
                .password("Password123!")
                .phoneNumber("9876543211")
                .role(Role.DONOR)
                .active(true)
                .emailVerified(true)
                .latitude(12.9720)
                .longitude(77.5950)
                .build());

        donorB = donorProfileRepository.save(DonorProfile.builder()
                .user(donorUserB)
                .email(donorUserB.getEmail())
                .bloodGroup(BloodGroup.B_POSITIVE)
                .age(28)
                .gender(com.bloodbridge.enums.Gender.MALE)
                .weight(74.0)
                .city("Bangalore")
                .state("Karnataka")
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .latitude(12.9720)
                .longitude(77.5950)
                .build());

        // Donor 2: O+ (Universal/Compatible for B+)
        donorUserO = userRepository.save(User.builder()
                .fullName("Sneha Reddy")
                .email("sneha.o@donor.com")
                .password("Password123!")
                .phoneNumber("9876543212")
                .role(Role.DONOR)
                .active(true)
                .emailVerified(true)
                .latitude(12.9725)
                .longitude(77.5955)
                .build());

        donorO = donorProfileRepository.save(DonorProfile.builder()
                .user(donorUserO)
                .email(donorUserO.getEmail())
                .bloodGroup(BloodGroup.O_POSITIVE)
                .age(26)
                .gender(com.bloodbridge.enums.Gender.FEMALE)
                .weight(62.0)
                .city("Bangalore")
                .state("Karnataka")
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .latitude(12.9725)
                .longitude(77.5955)
                .build());

        // Donor 3: A+ (Incompatible for B+)
        donorUserA = userRepository.save(User.builder()
                .fullName("Karan Singh")
                .email("karan.a@donor.com")
                .password("Password123!")
                .phoneNumber("9876543213")
                .role(Role.DONOR)
                .active(true)
                .emailVerified(true)
                .latitude(12.9730)
                .longitude(77.5960)
                .build());

        donorA = donorProfileRepository.save(DonorProfile.builder()
                .user(donorUserA)
                .email(donorUserA.getEmail())
                .bloodGroup(BloodGroup.A_POSITIVE)
                .age(30)
                .gender(com.bloodbridge.enums.Gender.MALE)
                .weight(80.0)
                .city("Bangalore")
                .state("Karnataka")
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .latitude(12.9730)
                .longitude(77.5960)
                .build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Complete Enterprise Emergency Blood Request Lifecycle & Security Verification Test")
    void testEnterpriseLifecycleAndSecurity() {
        // 1. Hospital creates emergency B+ blood request for 2 units
        HospitalBloodRequestCreate requestDto = new HospitalBloodRequestCreate();
        requestDto.setBloodGroupNeeded(BloodGroup.B_POSITIVE);
        requestDto.setUnitsRequired(2);
        requestDto.setUrgencyLevel(UrgencyLevel.CRITICAL);
        requestDto.setRequiredByDate(LocalDate.now().plusDays(1));
        requestDto.setReason("Trauma Emergency");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(hospitalUser1.getEmail(), null, List.of(() -> "ROLE_HOSPITAL"))
        );

        ApiResponse<BloodRequestResponse> createResp = hospitalService.createBloodRequest(hospitalUser1.getEmail(), requestDto);
        assertThat(createResp.isSuccess()).isTrue();
        Long requestId = createResp.getData().getId();

        // 2 & 3 & 4. Smart donor matching engine executes & creates MatchedEmergencyDonor records
        List<MatchedEmergencyDonor> matchedRecords = matchedEmergencyDonorRepository.findByBloodRequestId(requestId);
        List<Long> matchedDonorIds = matchedRecords.stream().map(m -> m.getDonor().getId()).toList();

        // Verify compatibility: B+ and O+ matched; A+ strictly excluded
        assertThat(matchedDonorIds).contains(donorB.getId(), donorO.getId());
        assertThat(matchedDonorIds).doesNotContain(donorA.getId());

        // 5 & 6. Donor 1 (B+) receives request & accepts
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(donorUserB.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );
        List<DonorEmergencyRequestDTO> donorBFeed = bloodRequestService.getMatchedEmergencyRequestsForDonor();
        assertThat(donorBFeed).isNotEmpty();
        assertThat(donorBFeed.stream().anyMatch(r -> r.getRequestId().equals(requestId))).isTrue();

        DonorEmergencyRequestDTO acceptRespB = bloodRequestService.acceptMatchedEmergencyRequest(requestId);
        assertThat(acceptRespB.getStatus()).isEqualTo("ACCEPTED");

        // 7, 8, 9. Hospital 1 views responses
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(hospitalUser1.getEmail(), null, List.of(() -> "ROLE_HOSPITAL"))
        );
        ApiResponse<HospitalEmergencyResponsesContainerDTO> responsesResp = hospitalService.getEmergencyRequestResponses(hospitalUser1.getEmail(), requestId);
        assertThat(responsesResp.isSuccess()).isTrue();
        HospitalEmergencyResponsesContainerDTO container = responsesResp.getData();
        assertThat(container.getAcceptedDonors()).isGreaterThanOrEqualTo(1);

        HospitalDonorResponseDTO bDonorDTO = container.getResponses().stream()
                .filter(r -> r.getDonorId().equals(donorB.getId()))
                .findFirst().orElseThrow();

        assertThat(bDonorDTO.getDonorName()).isEqualTo("Rohan Verma");
        assertThat(bDonorDTO.getBloodGroup()).isEqualTo("B+");
        assertThat(bDonorDTO.getResponseStatus()).isEqualTo("ACCEPTED");

        // 10 & 11 & 12. Hospital confirms Donor 1 -> Request transitions to FULFILLMENT_IN_PROGRESS
        ApiResponse<HospitalDonorResponseDTO> confirmResp = hospitalService.confirmDonor(hospitalUser1.getEmail(), requestId, bDonorDTO.getMatchedDonorId());
        assertThat(confirmResp.isSuccess()).isTrue();
        assertThat(confirmResp.getData().getConfirmed()).isTrue();

        BloodRequest inProgressReq = bloodRequestRepository.findById(requestId).orElseThrow();
        assertThat(inProgressReq.getStatus()).isEqualTo(RequestStatus.FULFILLMENT_IN_PROGRESS);

        // Donor B sees confirmation banner
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(donorUserB.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );
        List<DonorEmergencyRequestDTO> confirmedFeed = bloodRequestService.getMatchedEmergencyRequestsForDonor();
        assertThat(confirmedFeed.get(0).getConfirmed()).isTrue();
        assertThat(confirmedFeed.get(0).getRequestStatus()).isEqualTo("FULFILLMENT_IN_PROGRESS");

        // 13. Hospital completes emergency request
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(hospitalUser1.getEmail(), null, List.of(() -> "ROLE_HOSPITAL"))
        );
        ApiResponse<BloodRequestResponse> completeResp = hospitalService.completeEmergencyRequest(hospitalUser1.getEmail(), requestId);
        assertThat(completeResp.isSuccess()).isTrue();
        assertThat(completeResp.getData().getStatus()).isEqualTo(RequestStatus.COMPLETED);

        // 14. Completed request cannot be accepted again
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(donorUserO.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );
        assertThrows(InvalidRequestStateException.class, () -> bloodRequestService.acceptMatchedEmergencyRequest(requestId));

        // 16. Unauthorized hospital cannot view or modify another hospital's request
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(hospitalUser2.getEmail(), null, List.of(() -> "ROLE_HOSPITAL"))
        );
        assertThrows(AccessDeniedException.class, () -> hospitalService.getEmergencyRequestResponses(hospitalUser2.getEmail(), requestId));
        assertThrows(AccessDeniedException.class, () -> hospitalService.confirmDonor(hospitalUser2.getEmail(), requestId, bDonorDTO.getMatchedDonorId()));
    }
}
