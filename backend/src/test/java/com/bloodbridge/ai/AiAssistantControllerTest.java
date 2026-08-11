package com.bloodbridge.ai;

import com.bloodbridge.ai.dto.AiAssistantRequestDTO;
import com.bloodbridge.ai.dto.AiAssistantResponseDTO;
import com.bloodbridge.ai.dto.GroqApiRequestDTO;
import com.bloodbridge.ai.dto.GroqApiResponseDTO;
import com.bloodbridge.ai.service.impl.AiAssistantServiceImpl;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.Role;
import com.bloodbridge.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.web.client.RestClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AiAssistantControllerTest {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AiAssistantServiceImpl aiAssistantService;

    @MockBean
    private RestClient groqRestClient;

    @MockBean
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        testUser = User.builder()
                .id(100L)
                .email("hospital.admin@test.com")
                .fullName("St. Jude Hospital")
                .role(Role.HOSPITAL)
                .active(true)
                .build();

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("1. Successful AI Request - Valid chat prompt returns Groq response mapped payload")
    void testSuccessfulAiRequest() throws Exception {
        // Set test API key & model
        ReflectionTestUtils.setField(aiAssistantService, "apiKey", "gsk_mock_test_key_12345");
        ReflectionTestUtils.setField(aiAssistantService, "model", "llama-3.3-70b-versatile");

        // Mock RestClient call chain
        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(groqRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(GroqApiRequestDTO.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        GroqApiResponseDTO mockGroqResp = GroqApiResponseDTO.builder()
                .choices(List.of(
                        GroqApiResponseDTO.Choice.builder()
                                .message(com.bloodbridge.ai.dto.GroqApiRequestDTO.ChatMessage.builder()
                                        .role("assistant")
                                        .content("Donor matching works using a 10-stage medical compatibility and distance matrix algorithm.")
                                        .build())
                                .build()
                ))
                .build();

        when(responseSpec.body(GroqApiResponseDTO.class)).thenReturn(mockGroqResp);

        // Authenticate user
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser.getEmail(), null, List.of(() -> "ROLE_HOSPITAL"))
        );

        AiAssistantRequestDTO req = new AiAssistantRequestDTO("How does donor matching work?");

        mockMvc.perform(post("/api/v1/ai/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("AI response generated successfully"))
                .andExpect(jsonPath("$.data.reply").value("Donor matching works using a 10-stage medical compatibility and distance matrix algorithm."));
    }

    @Test
    @DisplayName("2. Missing API Key - Gracefully returns temporary unavailable message without crashing")
    void testMissingApiKey() throws Exception {
        ReflectionTestUtils.setField(aiAssistantService, "apiKey", "");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser.getEmail(), null, List.of(() -> "ROLE_HOSPITAL"))
        );

        AiAssistantRequestDTO req = new AiAssistantRequestDTO("Hello AI");

        ApiResponse<AiAssistantResponseDTO> response = aiAssistantService.processChat(testUser.getEmail(), req);
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(AiAssistantServiceImpl.FALLBACK_UNAVAILABLE_MESSAGE);
    }

    @Test
    @DisplayName("3. Groq API Failure - Exception during HTTP call returns clean fallback error")
    void testGroqApiFailure() throws Exception {
        ReflectionTestUtils.setField(aiAssistantService, "apiKey", "gsk_mock_test_key_12345");

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        when(groqRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenThrow(new RuntimeException("Connection timeout"));

        AiAssistantRequestDTO req = new AiAssistantRequestDTO("Is blood bank open?");
        ApiResponse<AiAssistantResponseDTO> response = aiAssistantService.processChat(testUser.getEmail(), req);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(AiAssistantServiceImpl.FALLBACK_UNAVAILABLE_MESSAGE);
    }

    @Test
    @DisplayName("4. Unauthorized Request - Unauthenticated call returns 401 Unauthorized")
    void testUnauthorizedRequest() throws Exception {
        AiAssistantRequestDTO req = new AiAssistantRequestDTO("How to donate?");

        mockMvc.perform(post("/api/v1/ai/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("5. Empty Message - Blank message returns 400 Bad Request")
    void testEmptyMessageValidation() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser.getEmail(), null, List.of(() -> "ROLE_HOSPITAL"))
        );

        AiAssistantRequestDTO req = new AiAssistantRequestDTO("   ");

        mockMvc.perform(post("/api/v1/ai/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("6. Very Long Message - Message exceeding 2000 characters returns 400 Bad Request")
    void testVeryLongMessageValidation() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser.getEmail(), null, List.of(() -> "ROLE_HOSPITAL"))
        );

        String longMsg = "a".repeat(2001);
        AiAssistantRequestDTO req = new AiAssistantRequestDTO(longMsg);

        mockMvc.perform(post("/api/v1/ai/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("7. Proper Response Mapping - Service correctly maps Groq choices content into reply DTO")
    void testProperResponseMapping() {
        ReflectionTestUtils.setField(aiAssistantService, "apiKey", "gsk_mock_test_key_12345");

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(groqRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(GroqApiRequestDTO.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        GroqApiResponseDTO mockGroqResp = GroqApiResponseDTO.builder()
                .choices(List.of(
                        GroqApiResponseDTO.Choice.builder()
                                .message(com.bloodbridge.ai.dto.GroqApiRequestDTO.ChatMessage.builder()
                                        .role("assistant")
                                        .content("   Welcome to BloodBridge! You can post emergency requests from your dashboard.   ")
                                        .build())
                                .build()
                ))
                .build();

        when(responseSpec.body(GroqApiResponseDTO.class)).thenReturn(mockGroqResp);

        AiAssistantRequestDTO req = new AiAssistantRequestDTO("Hello");
        ApiResponse<AiAssistantResponseDTO> result = aiAssistantService.processChat(testUser.getEmail(), req);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getReply()).isEqualTo("Welcome to BloodBridge! You can post emergency requests from your dashboard.");
    }
}
