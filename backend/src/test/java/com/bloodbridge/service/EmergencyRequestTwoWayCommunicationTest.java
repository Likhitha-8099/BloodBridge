package com.bloodbridge.service;

import com.bloodbridge.dto.request.HospitalBloodRequestCreate;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.response.DonorEmergencyRequestDTO;
import com.bloodbridge.dto.response.HospitalDonorResponseDTO;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.MatchedEmergencyDonorStatus;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.enums.Role;
import com.bloodbridge.enums.UrgencyLevel;
import com.bloodbridge.repository.*;
import org.junit.jupiter.api.AfterEach;
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
public class EmergencyRequestTwoWayCommunicationTest {

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
    private NotificationRepository notificationRepository;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private BloodRequestService bloodRequestService;

    private User hospitalUser;
    private User donorUserB;
    private DonorProfile donorProfileB;
    private User donorUserO;
    private DonorProfile donorProfileO;
    private User donorUserA;
    private DonorProfile donorProfileA;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        // 1. Hospital Account: Apollo Hospitals
        hospitalUser = userRepository.save(User.builder()
                .fullName("Apollo Hospitals Jubilee Hills")
                .email("apollo.twoway@hospital.com")
                .password("Password123!")
                .phoneNumber("08011223344")
                .role(Role.HOSPITAL)
                .active(true)
                .emailVerified(true)
                .latitude(17.4325)
                .longitude(78.4071)
                .build());

        hospitalRepository.save(Hospital.builder()
                .user(hospitalUser)
                .hospitalName("Apollo Hospitals")
                .email("apollo.twoway@hospital.com")
                .phoneNumber("08011223344")
                .registrationNumber("REG-APOLLO-TWOWAY")
                .address("Jubilee Hills")
                .city("Hyderabad")
                .state("Telangana")
                .latitude(17.4325)
                .longitude(78.4071)
                .verified(true)
                .verificationStatus("APPROVED")
                .status("ACTIVE")
                .build());

        // 2. Donor 1: B+ (Compatible)
        donorUserB = userRepository.save(User.builder()
                .fullName("Rahul Kumar")
                .email("rahul.b@donor.com")
                .password("Password123!")
                .phoneNumber("9876543201")
                .role(Role.DONOR)
                .active(true)
                .emailVerified(true)
                .latitude(17.4320)
                .longitude(78.4068)
                .build());

        donorProfileB = donorProfileRepository.save(DonorProfile.builder()
                .user(donorUserB)
                .email(donorUserB.getEmail())
                .bloodGroup(BloodGroup.B_POSITIVE)
                .age(28)
                .gender(com.bloodbridge.enums.Gender.MALE)
                .city("Hyderabad")
                .state("Telangana")
                .weight(74.0)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .latitude(17.4320)
                .longitude(78.4068)
                .build());

        // 3. Donor 2: O+ (Compatible)
        donorUserO = userRepository.save(User.builder()
                .fullName("Priya Sharma")
                .email("priya.o@donor.com")
                .password("Password123!")
                .phoneNumber("9876543202")
                .role(Role.DONOR)
                .active(true)
                .emailVerified(true)
                .latitude(17.4320)
                .longitude(78.4068)
                .build());

        donorProfileO = donorProfileRepository.save(DonorProfile.builder()
                .user(donorUserO)
                .email(donorUserO.getEmail())
                .bloodGroup(BloodGroup.O_POSITIVE)
                .age(26)
                .gender(com.bloodbridge.enums.Gender.FEMALE)
                .city("Hyderabad")
                .state("Telangana")
                .weight(62.0)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .latitude(17.4320)
                .longitude(78.4068)
                .build());

        // 4. Donor 3: A+ (Incompatible)
        donorUserA = userRepository.save(User.builder()
                .fullName("Amit Patel")
                .email("amit.a@donor.com")
                .password("Password123!")
                .phoneNumber("9876543203")
                .role(Role.DONOR)
                .active(true)
                .emailVerified(true)
                .latitude(17.4320)
                .longitude(78.4068)
                .build());

        donorProfileA = donorProfileRepository.save(DonorProfile.builder()
                .user(donorUserA)
                .email(donorUserA.getEmail())
                .bloodGroup(BloodGroup.A_POSITIVE)
                .age(30)
                .gender(com.bloodbridge.enums.Gender.MALE)
                .city("Hyderabad")
                .state("Telangana")
                .weight(80.0)
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .latitude(17.4320)
                .longitude(78.4068)
                .build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Complete Two-Way Emergency Communication Workflow Test")
    void testTwoWayEmergencyCommunicationWorkflow() {
        // Step 1: Hospital creates emergency B+ blood request
        HospitalBloodRequestCreate requestDto = new HospitalBloodRequestCreate();
        requestDto.setBloodGroupNeeded(BloodGroup.B_POSITIVE);
        requestDto.setUnitsRequired(1);
        requestDto.setUrgencyLevel(UrgencyLevel.HIGH);
        requestDto.setRequiredByDate(LocalDate.now().plusDays(1));
        requestDto.setReason("Emergency Surgery");

        ApiResponse<BloodRequestResponse> createResp = hospitalService.createBloodRequest(hospitalUser.getEmail(), requestDto);
        assertThat(createResp.isSuccess()).isTrue();
        Long requestId = createResp.getData().getId();

        // Step 2 & 3: Matching pipeline executes & persists matched donors
        List<MatchedEmergencyDonor> matchedRecords = matchedEmergencyDonorRepository.findByBloodRequestId(requestId);
        List<Long> matchedDonorIds = matchedRecords.stream().map(m -> m.getDonor().getId()).toList();

        // Verify compatibility: B+ and O+ matched; A+ strictly excluded
        assertThat(matchedDonorIds).contains(donorProfileB.getId(), donorProfileO.getId());
        assertThat(matchedDonorIds).doesNotContain(donorProfileA.getId());

        // Step 4: Donor 1 (B+) opens /donor/requests & calls API
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(donorUserB.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );

        List<DonorEmergencyRequestDTO> donorBFeed = bloodRequestService.getMatchedEmergencyRequestsForDonor();
        assertThat(donorBFeed).isNotEmpty();
        assertThat(donorBFeed.get(0).getRequestId()).isEqualTo(requestId);
        assertThat(donorBFeed.get(0).getHospitalName()).isEqualTo("Apollo Hospitals");

        // Step 5: Donor 1 (B+) clicks ACCEPT
        DonorEmergencyRequestDTO acceptResult = bloodRequestService.acceptMatchedEmergencyRequest(requestId);
        assertThat(acceptResult.getStatus()).isEqualTo("ACCEPTED");

        // Verify matched_emergency_donors row status & acceptedAt timestamp
        MatchedEmergencyDonor updatedMedB = matchedEmergencyDonorRepository
                .findByBloodRequestIdAndDonorId(requestId, donorProfileB.getId()).orElseThrow();
        assertThat(updatedMedB.getStatus()).isEqualTo(MatchedEmergencyDonorStatus.ACCEPTED);
        assertThat(updatedMedB.getAcceptedAt()).isNotNull();

        // Step 6: Verify Idempotency on repeated ACCEPT call
        DonorEmergencyRequestDTO repeatAcceptResult = bloodRequestService.acceptMatchedEmergencyRequest(requestId);
        assertThat(repeatAcceptResult.getStatus()).isEqualTo("ACCEPTED");

        // Step 7: Verify Hospital In-App Notification created for Hospital User
        List<Notification> hospitalNotifications = notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(hospitalUser.getId());
        assertThat(hospitalNotifications).isNotEmpty();
        assertThat(hospitalNotifications.get(0).getTitle()).contains("Donor Accepted");

        // Step 8: Authenticate as Hospital & Call GET /api/v1/hospital/emergency-requests/{requestId}/responses
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(hospitalUser.getEmail(), null, List.of(() -> "ROLE_HOSPITAL"))
        );

        ApiResponse<com.bloodbridge.dto.response.HospitalEmergencyResponsesContainerDTO> responsesResult = hospitalService.getEmergencyRequestResponses(hospitalUser.getEmail(), requestId);
        assertThat(responsesResult.isSuccess()).isTrue();
        com.bloodbridge.dto.response.HospitalEmergencyResponsesContainerDTO container = responsesResult.getData();
        assertThat(container).isNotNull();
        assertThat(container.getAcceptedDonors()).isGreaterThanOrEqualTo(1);
        assertThat(container.getPendingDonors()).isGreaterThanOrEqualTo(1);

        List<HospitalDonorResponseDTO> responsesList = container.getResponses();
        assertThat(responsesList).isNotEmpty();

        HospitalDonorResponseDTO bDonorResponse = responsesList.stream()
                .filter(r -> r.getDonorId().equals(donorProfileB.getId()))
                .findFirst().orElseThrow();

        assertThat(bDonorResponse.getDonorName()).isEqualTo("Rahul Kumar");
        assertThat(bDonorResponse.getBloodGroup()).isEqualTo("B+");
        assertThat(bDonorResponse.getResponseStatus()).isEqualTo("ACCEPTED");
        assertThat(bDonorResponse.getAcceptedAt()).isNotNull();
        assertThat(bDonorResponse.getEmail()).isEqualTo("rahul.b@donor.com");

        HospitalDonorResponseDTO oDonorResponse = responsesList.stream()
                .filter(r -> r.getDonorId().equals(donorProfileO.getId()))
                .findFirst().orElseThrow();

        assertThat(oDonorResponse.getResponseStatus()).isEqualTo("PENDING");

        // Step 9: Hospital confirms Donor 1 (Rahul Kumar / B+)
        ApiResponse<com.bloodbridge.dto.response.HospitalDonorResponseDTO> confirmResp = hospitalService.confirmDonor(hospitalUser.getEmail(), requestId, bDonorResponse.getMatchedDonorId());
        assertThat(confirmResp.isSuccess()).isTrue();
        assertThat(confirmResp.getData().getConfirmed()).isTrue();
        assertThat(confirmResp.getData().getResponseStatus()).isEqualTo("CONFIRMED");

        // Verify request state changed to FULFILLMENT_IN_PROGRESS
        BloodRequest updatedReq = bloodRequestRepository.findById(requestId).orElseThrow();
        assertThat(updatedReq.getStatus()).isEqualTo(RequestStatus.FULFILLMENT_IN_PROGRESS);

        // Step 10: Donor 1 checks emergency request feed & sees confirmation
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(donorUserB.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );
        List<DonorEmergencyRequestDTO> donorFeedConfirmed = bloodRequestService.getMatchedEmergencyRequestsForDonor();
        assertThat(donorFeedConfirmed).isNotEmpty();
        assertThat(donorFeedConfirmed.get(0).getConfirmed()).isTrue();
        assertThat(donorFeedConfirmed.get(0).getRequestStatus()).isEqualTo("FULFILLMENT_IN_PROGRESS");

        // Step 11: Hospital completes the emergency blood request
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(hospitalUser.getEmail(), null, List.of(() -> "ROLE_HOSPITAL"))
        );
        ApiResponse<BloodRequestResponse> completeResp = hospitalService.completeEmergencyRequest(hospitalUser.getEmail(), requestId);
        assertThat(completeResp.isSuccess()).isTrue();
        assertThat(completeResp.getData().getStatus()).isEqualTo(RequestStatus.COMPLETED);

        // Step 12: Completed request cannot be accepted again
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(donorUserO.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );
        org.junit.jupiter.api.Assertions.assertThrows(
                com.bloodbridge.exception.InvalidRequestStateException.class,
                () -> bloodRequestService.acceptMatchedEmergencyRequest(requestId)
        );
    }
}
