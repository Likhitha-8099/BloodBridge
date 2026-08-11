package com.bloodbridge.ai;

import com.bloodbridge.ai.dto.AiAssistantRequestDTO;
import com.bloodbridge.ai.dto.AiAssistantResponseDTO;
import com.bloodbridge.ai.dto.GroqApiRequestDTO;
import com.bloodbridge.ai.dto.GroqApiResponseDTO;
import com.bloodbridge.ai.service.impl.AiAssistantServiceImpl;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.MatchedEmergencyDonor;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.MatchedEmergencyDonorStatus;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.enums.Role;
import com.bloodbridge.enums.UrgencyLevel;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AiAssistantRoleAwareTest {

    @Autowired
    private AiAssistantServiceImpl aiAssistantService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DonorProfileRepository donorProfileRepository;

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private MatchedEmergencyDonorRepository matchedEmergencyDonorRepository;

    @MockBean
    private RestClient groqRestClient;

    private User adminUser;
    private User hospitalUser;
    private Hospital hospital;
    private User donorUser;
    private DonorProfile donorProfile;
    private User unauthorizedDonorUser;
    private BloodRequest bloodRequest;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        ReflectionTestUtils.setField(aiAssistantService, "apiKey", "gsk_mock_role_test_key_999");

        // Admin User
        adminUser = userRepository.save(User.builder()
                .fullName("System Administrator")
                .email("admin.ai@test.com")
                .password("Password123!")
                .phoneNumber("9999911111")
                .role(Role.ADMIN)
                .active(true)
                .emailVerified(true)
                .build());

        // Hospital User & Hospital Entity
        hospitalUser = userRepository.save(User.builder()
                .fullName("St. Jude Hospital")
                .email("stjude.ai@test.com")
                .password("Password123!")
                .phoneNumber("9999922222")
                .role(Role.HOSPITAL)
                .active(true)
                .emailVerified(true)
                .build());

        hospital = hospitalRepository.save(Hospital.builder()
                .user(hospitalUser)
                .hospitalName("St. Jude Hospital")
                .email(hospitalUser.getEmail())
                .phoneNumber("9999922222")
                .registrationNumber("REG-STJUDE-99")
                .address("77 Medical Square")
                .city("Bangalore")
                .state("Karnataka")
                .verified(true)
                .verificationStatus("APPROVED")
                .status("ACTIVE")
                .build());

        // Matched Donor User & Profile
        donorUser = userRepository.save(User.builder()
                .fullName("Rahul Verma")
                .email("rahul.ai@donor.com")
                .password("Password123!")
                .phoneNumber("9999933333")
                .role(Role.DONOR)
                .active(true)
                .emailVerified(true)
                .build());

        donorProfile = donorProfileRepository.save(DonorProfile.builder()
                .user(donorUser)
                .email(donorUser.getEmail())
                .bloodGroup(BloodGroup.B_POSITIVE)
                .age(28)
                .gender(com.bloodbridge.enums.Gender.MALE)
                .weight(75.0)
                .city("Bangalore")
                .state("Karnataka")
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .build());

        // Unauthorized Donor User & Profile
        unauthorizedDonorUser = userRepository.save(User.builder()
                .fullName("Vikram Seth")
                .email("vikram.unauth@donor.com")
                .password("Password123!")
                .phoneNumber("9999944444")
                .role(Role.DONOR)
                .active(true)
                .emailVerified(true)
                .build());

        donorProfileRepository.save(DonorProfile.builder()
                .user(unauthorizedDonorUser)
                .email(unauthorizedDonorUser.getEmail())
                .bloodGroup(BloodGroup.A_POSITIVE)
                .age(32)
                .gender(com.bloodbridge.enums.Gender.MALE)
                .weight(82.0)
                .city("Bangalore")
                .state("Karnataka")
                .availableForDonation(true)
                .emergencyAvailable(true)
                .status("ACTIVE")
                .verificationStatus("VERIFIED")
                .build());

        // Blood Request
        bloodRequest = bloodRequestRepository.save(BloodRequest.builder()
                .hospital(hospital)
                .bloodGroupNeeded(BloodGroup.B_POSITIVE)
                .unitsRequired(2)
                .urgencyLevel(UrgencyLevel.CRITICAL)
                .requestDate(java.time.LocalDateTime.now())
                .requiredByDate(LocalDate.now().plusDays(1))
                .reason("Accident Emergency")
                .status(RequestStatus.ACTIVE)
                .build());

        // Matched Emergency Donor Record
        matchedEmergencyDonorRepository.save(MatchedEmergencyDonor.builder()
                .bloodRequest(bloodRequest)
                .donor(donorProfile)
                .hospital(hospital)
                .distanceKm(12.5)
                .matchingGroup("Group A (0-50km)")
                .status(MatchedEmergencyDonorStatus.ACCEPTED)
                .build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private GroqApiRequestDTO lastCapturedGroqPayload;

    private void mockGroqResponse(String expectedAiReply) {
        lastCapturedGroqPayload = null;
        ReflectionTestUtils.setField(aiAssistantService, "apiKey", "gsk_mock_role_test_key_999");
        ReflectionTestUtils.setField(aiAssistantService, "model", "llama-3.3-70b-versatile");

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class, org.mockito.Mockito.RETURNS_SELF);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(groqRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        GroqApiResponseDTO mockGroqResp = GroqApiResponseDTO.builder()
                .choices(List.of(
                        GroqApiResponseDTO.Choice.builder()
                                .message(GroqApiRequestDTO.ChatMessage.builder()
                                        .role("assistant")
                                        .content(expectedAiReply)
                                        .build())
                                .build()
                ))
                .build();

        when(responseSpec.body(GroqApiResponseDTO.class)).thenReturn(mockGroqResp);

        org.mockito.Mockito.doAnswer(inv -> {
            lastCapturedGroqPayload = inv.getArgument(0, GroqApiRequestDTO.class);
            return requestBodySpec;
        }).when(requestBodySpec).body(any(GroqApiRequestDTO.class));
    }

    @Test
    @DisplayName("1. ADMIN: 'How does hospital approval work?' - Injects ADMIN role context and verification guidance")
    void testAdminHospitalApprovalQuestion() {
        mockGroqResponse("Admin approves hospitals via the Admin Verification Dashboard by inspecting medical license documents.");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminUser.getEmail(), null, List.of(() -> "ROLE_ADMIN"))
        );

        AiAssistantRequestDTO req = AiAssistantRequestDTO.builder()
                .message("How does hospital approval work?")
                .currentPage("/admin/hospitals")
                .build();

        ApiResponse<AiAssistantResponseDTO> resp = aiAssistantService.processChat(adminUser.getEmail(), req);
        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getData().getReply()).contains("Admin approves hospitals");

        assertThat(lastCapturedGroqPayload).isNotNull();
        String systemPrompt = lastCapturedGroqPayload.getMessages().get(0).getContent();
        assertThat(systemPrompt).contains("\"role\":\"ADMIN\"");
        assertThat(systemPrompt).contains("[ADMIN ROLE]");
    }

    @Test
    @DisplayName("2. HOSPITAL: 'How does donor matching work?' - Injects HOSPITAL role context and 10-stage smart matching guidance")
    void testHospitalDonorMatchingQuestion() {
        mockGroqResponse("Donor matching evaluates 10 stages including blood group compatibility, 90-day cooldown, and distance grouping (Group A-D).");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(hospitalUser.getEmail(), null, List.of(() -> "ROLE_HOSPITAL"))
        );

        AiAssistantRequestDTO req = AiAssistantRequestDTO.builder()
                .message("How does donor matching work?")
                .currentPage("/hospital/dashboard")
                .requestId(bloodRequest.getId())
                .build();

        ApiResponse<AiAssistantResponseDTO> resp = aiAssistantService.processChat(hospitalUser.getEmail(), req);
        assertThat(resp.isSuccess()).isTrue();

        assertThat(lastCapturedGroqPayload).isNotNull();
        String systemPrompt = lastCapturedGroqPayload.getMessages().get(0).getContent();
        assertThat(systemPrompt).contains("\"role\":\"HOSPITAL\"");
        assertThat(systemPrompt).contains("St. Jude Hospital");
        assertThat(systemPrompt).contains("\"requestId\":" + bloodRequest.getId());
    }

    @Test
    @DisplayName("3. DONOR: 'Why did I receive this request?' - Injects DONOR role and authorized matched request context")
    void testDonorWhyReceivedRequestQuestion() {
        mockGroqResponse("You received Emergency Request #1 because your blood group B+ is compatible and you are within 12.5 KM (Group A).");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(donorUser.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );

        AiAssistantRequestDTO req = AiAssistantRequestDTO.builder()
                .message("Why did I receive this request?")
                .currentPage("/donor/feed")
                .requestId(bloodRequest.getId())
                .build();

        ApiResponse<AiAssistantResponseDTO> resp = aiAssistantService.processChat(donorUser.getEmail(), req);
        assertThat(resp.isSuccess()).isTrue();

        assertThat(lastCapturedGroqPayload).isNotNull();
        String systemPrompt = lastCapturedGroqPayload.getMessages().get(0).getContent();
        assertThat(systemPrompt).contains("\"role\":\"DONOR\"");
        assertThat(systemPrompt).contains("\"bloodGroup\":\"B_POSITIVE\"");
        assertThat(systemPrompt).contains("\"distanceKm\":12.5");
        assertThat(systemPrompt).contains("\"myResponseStatus\":\"ACCEPTED\"");
    }

    @Test
    @DisplayName("4. DONOR: 'What happens after I accept?' - Explains post-acceptance hospital confirmation workflow")
    void testDonorWhatHappensAfterAccept() {
        mockGroqResponse("After accepting, your status updates to 'Waiting for hospital confirmation'. Once confirmed, you get hospital phone and navigation.");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(donorUser.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );

        AiAssistantRequestDTO req = AiAssistantRequestDTO.builder()
                .message("What happens after I accept?")
                .currentPage("/donor/feed")
                .build();

        ApiResponse<AiAssistantResponseDTO> resp = aiAssistantService.processChat(donorUser.getEmail(), req);
        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getData().getReply()).contains("Waiting for hospital confirmation");
    }

    @Test
    @DisplayName("5. HOSPITAL: 'What happens when a donor accepts?' - Explains hospital response tracking and confirmation actions")
    void testHospitalWhatHappensWhenDonorAccepts() {
        mockGroqResponse("When a donor accepts, they appear under Accepted Donors on your dashboard. Click 'Select & Confirm Donor' to trigger fulfillment.");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(hospitalUser.getEmail(), null, List.of(() -> "ROLE_HOSPITAL"))
        );

        AiAssistantRequestDTO req = AiAssistantRequestDTO.builder()
                .message("What happens when a donor accepts?")
                .currentPage("/hospital/requests")
                .build();

        ApiResponse<AiAssistantResponseDTO> resp = aiAssistantService.processChat(hospitalUser.getEmail(), req);
        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getData().getReply()).contains("Select & Confirm Donor");
    }

    @Test
    @DisplayName("6. Security Scoping: Unauthorized donor asking about another donor's request context cannot access request data")
    void testUnauthorizedDonorCannotAccessAnotherUsersRequestContext() {
        mockGroqResponse("I can explain general blood donation rules for you.");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(unauthorizedDonorUser.getEmail(), null, List.of(() -> "ROLE_DONOR"))
        );

        AiAssistantRequestDTO req = AiAssistantRequestDTO.builder()
                .message("What are the details of this request?")
                .requestId(bloodRequest.getId()) // Request not matched to unauthorized donor
                .build();

        ApiResponse<AiAssistantResponseDTO> resp = aiAssistantService.processChat(unauthorizedDonorUser.getEmail(), req);
        assertThat(resp.isSuccess()).isTrue();

        assertThat(lastCapturedGroqPayload).isNotNull();
        String systemPrompt = lastCapturedGroqPayload.getMessages().get(0).getContent();
        // Context must NOT contain activeRequestContext because unauthorized donor was not matched to bloodRequest
        assertThat(systemPrompt).doesNotContain("\"activeRequestContext\"");
    }
}
