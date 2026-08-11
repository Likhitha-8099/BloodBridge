package com.bloodbridge.ai.service.impl;

import com.bloodbridge.ai.dto.AiAssistantRequestDTO;
import com.bloodbridge.ai.dto.AiAssistantResponseDTO;
import com.bloodbridge.ai.dto.GroqApiRequestDTO;
import com.bloodbridge.ai.dto.GroqApiResponseDTO;
import com.bloodbridge.ai.dto.UserAiContextDTO;
import com.bloodbridge.ai.service.AiAssistantService;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.MatchedEmergencyDonor;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.Role;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.MatchedEmergencyDonorRepository;
import com.bloodbridge.repository.PatientProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link AiAssistantService} executing OpenAI-compatible
 * chat completions against Groq API with role-aware knowledge, authorized user
 * context, robust error handling, and security guards.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final RestClient groqRestClient;
    private final UserRepository userRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final MatchedEmergencyDonorRepository matchedEmergencyDonorRepository;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${groq.temperature:0.7}")
    private Double temperature;

    @Value("${groq.max-tokens:1024}")
    private Integer maxTokens;

    public static final String FALLBACK_UNAVAILABLE_MESSAGE = "BloodBridge AI Assistant is temporarily unavailable.";

    private static final String MASTER_SYSTEM_PROMPT = """
            You are the BloodBridge AI Assistant, an intelligent guide for the BloodBridge blood donation and emergency blood request platform.

            ================================================================================
            BLOODBRIDGE SYSTEM WORKFLOW KNOWLEDGE
            ================================================================================
            1. Registration & Auth: Users register as DONOR, HOSPITAL, PATIENT, or ADMIN.
            2. Hospital Verification: Admin reviews registered hospitals and sets status to APPROVED or REJECTED. Only APPROVED hospitals can post emergency blood requests.
            3. Emergency Blood Request Creation: Hospitals post emergency requests specifying Blood Group Needed, Units Required, Urgency Level (CRITICAL, HIGH, MEDIUM), Required-by Date, and Reason.
            4. 10-Stage Smart Donor Matching Engine:
               - Stage 1: Active donor filter.
               - Stage 2: Medical Blood Group Compatibility Check:
                 * O- (Universal donor): Can donate to O-, O+, A-, A+, B-, B+, AB-, AB+.
                 * O+: Can donate to O+, A+, B+, AB+.
                 * B+: Can receive from B+, B-, O+, O-. Can donate to B+, AB+.
                 * B-: Can receive from B-, O-.
                 * A+: Can receive from A+, A-, O+, O-.
                 * AB+: Universal recipient.
               - Stage 3: Eligibility & Cooldown Filter: Donors must be active, verified, emergency-available, aged 18-65, weight >= 50kg, and past the 90-day donation cooldown period.
               - Stage 4: Distance Calculation & Priority Grouping:
                 * Group A: 0 – 50 KM (Immediate Primary)
                 * Group B: 50 – 75 KM (Secondary)
                 * Group C: 75 – 100 KM (Tertiary)
                 * Group D: > 100 KM (Extended Regional)
               - Stage 5-10: Assignment, notification dispatch, and real-time analytics.
            5. Donor Response & Confirmation Lifecycle:
               - Donors receive real-time in-app notifications and email alerts.
               - Donors view requests on their Emergency Requests Feed.
               - Donor accepts or declines: Accepting sets donor status to ACCEPTED ("Waiting for hospital confirmation").
               - Hospital reviews responses and clicks "Select & Confirm Donor" -> updates status to CONFIRMED / FULFILLMENT_IN_PROGRESS ("Selected for emergency").
               - Donor views hospital contact phone, hospital address, navigation button ("📍 Navigate to Hospital"), and fulfillment instructions.
               - Hospital completes request ("Complete Request") upon successful donation delivery.

            ================================================================================
            ROLE-SPECIFIC KNOWLEDGE & GUIDANCE
            ================================================================================
            [ADMIN ROLE]
            - Explain hospital verification/approval/rejection (/admin/hospitals), user role administration, platform metrics, audit logs, and system analytics.

            [HOSPITAL ROLE]
            - Explain creating emergency requests, selecting required blood group & units, 10-stage smart donor matching, distance priority groups (Group A-D), tracking live donor responses (Accepted/Pending/Rejected), selecting & confirming donors, and completing requests.

            [DONOR ROLE]
            - Explain donor blood group compatibility, eligibility rules (90-day cooldown, weight > 50kg, age 18-65), emergency feed, accepting/declining requests, status updates ("Waiting for hospital confirmation" vs "Selected for emergency"), and hospital contact & navigation instructions.

            [PATIENT ROLE]
            - Explain patient registration, patient profile details, creating patient blood requests, viewing active request status, and request cancellation rules.

            ================================================================================
            STRICT GUARDRAILS & BOUNDARIES
            ================================================================================
            1. DO NOT make business decisions: You MUST NOT decide medical eligibility, compatibility, hospital approval, or request acceptance. Those decisions are strictly handled by backend business logic.
            2. DO NOT expose sensitive data: Never reveal passwords, JWT tokens, API keys, database credentials, or private information belonging to unauthorized users.
            3. Respect user role: Provide answers tailored strictly to the authenticated user's role and authorized context provided below.
            4. Medical Disclaimer: You are an application assistant, NOT a medical professional. For medical emergencies, recommend contacting qualified healthcare professionals or emergency services.

            ================================================================================
            AUTHENTICATED USER CONTEXT (JSON)
            ================================================================================
            %s
            """;

    @Override
    public ApiResponse<AiAssistantResponseDTO> processChat(String userEmail, AiAssistantRequestDTO request) {
        log.info("[AI-ASSISTANT] Request received from user: {}", userEmail);

        if (!StringUtils.hasText(apiKey)) {
            log.warn("[AI-ASSISTANT] Groq API key is missing or not configured.");
            return ApiResponse.error(FALLBACK_UNAVAILABLE_MESSAGE);
        }

        if (request == null || !StringUtils.hasText(request.getMessage())) {
            return ApiResponse.error("Message cannot be empty");
        }

        Optional<User> userOpt = StringUtils.hasText(userEmail) ? userRepository.findByEmail(userEmail) : Optional.empty();
        User user = userOpt.orElse(null);

        UserAiContextDTO contextDTO = buildUserContext(user, request.getCurrentPage(), request.getRequestId());
        String contextJson;
        try {
            contextJson = objectMapper.writeValueAsString(contextDTO);
        } catch (Exception e) {
            contextJson = "{\"role\":\"" + (user != null && user.getRole() != null ? user.getRole().name() : "GUEST") + "\"}";
        }

        String systemPrompt = String.format(MASTER_SYSTEM_PROMPT, contextJson);

        GroqApiRequestDTO groqPayload = GroqApiRequestDTO.builder()
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .messages(List.of(
                        GroqApiRequestDTO.ChatMessage.builder().role("system").content(systemPrompt).build(),
                        GroqApiRequestDTO.ChatMessage.builder().role("user").content(request.getMessage()).build()
                ))
                .build();

        try {
            log.info("[AI-ASSISTANT] Groq request started using model: {}", model);

            GroqApiResponseDTO response = groqRestClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(groqPayload)
                    .retrieve()
                    .body(GroqApiResponseDTO.class);

            log.info("[AI-ASSISTANT] Groq response received successfully");

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                GroqApiRequestDTO.ChatMessage replyMsg = response.getChoices().get(0).getMessage();
                if (replyMsg != null && StringUtils.hasText(replyMsg.getContent())) {
                    log.info("[AI-ASSISTANT] Request completed");
                    AiAssistantResponseDTO responseDTO = AiAssistantResponseDTO.builder()
                            .reply(replyMsg.getContent().trim())
                            .build();
                    return ApiResponse.success("AI response generated successfully", responseDTO);
                }
            }

            log.warn("[AI-ASSISTANT] Groq API returned empty or malformed completion choices.");
            return ApiResponse.error(FALLBACK_UNAVAILABLE_MESSAGE);

        } catch (Exception ex) {
            System.err.println("GROQ_PROCESS_CHAT_EXCEPTION: " + ex.getClass().getName() + ": " + ex.getMessage());
            ex.printStackTrace();
            log.error("[AI-ASSISTANT] Groq API request failed: {}", ex.getMessage(), ex);
            return ApiResponse.error(FALLBACK_UNAVAILABLE_MESSAGE);
        }
    }

    private UserAiContextDTO buildUserContext(User user, String currentPage, Long requestId) {
        if (user == null) {
            return UserAiContextDTO.builder()
                    .role("GUEST")
                    .currentPage(currentPage)
                    .build();
        }

        Role role = user.getRole() != null ? user.getRole() : Role.DONOR;
        Map<String, Object> roleDetails = new HashMap<>();
        Map<String, Object> requestContext = new HashMap<>();

        if (role == Role.DONOR) {
            Optional<DonorProfile> donorOpt = donorProfileRepository.findByUserId(user.getId());
            if (donorOpt.isPresent()) {
                DonorProfile donor = donorOpt.get();
                roleDetails.put("bloodGroup", donor.getBloodGroup() != null ? donor.getBloodGroup().name() : "N/A");
                roleDetails.put("city", donor.getCity());
                roleDetails.put("availableForDonation", donor.getAvailableForDonation());
                roleDetails.put("emergencyAvailable", donor.getEmergencyAvailable());
                roleDetails.put("status", donor.getStatus());
                roleDetails.put("verificationStatus", donor.getVerificationStatus());

                if (requestId != null) {
                    List<MatchedEmergencyDonor> matches = matchedEmergencyDonorRepository.findByBloodRequestId(requestId);
                    Optional<MatchedEmergencyDonor> myMatch = matches.stream()
                            .filter(m -> m.getDonor() != null && m.getDonor().getId().equals(donor.getId()))
                            .findFirst();

                    if (myMatch.isPresent()) {
                        MatchedEmergencyDonor m = myMatch.get();
                        BloodRequest req = m.getBloodRequest();
                        requestContext.put("requestId", req.getId());
                        requestContext.put("bloodGroupNeeded", req.getBloodGroupNeeded() != null ? req.getBloodGroupNeeded().name() : "");
                        requestContext.put("unitsRequired", req.getUnitsRequired());
                        requestContext.put("urgencyLevel", req.getUrgencyLevel() != null ? req.getUrgencyLevel().name() : "");
                        requestContext.put("hospitalName", req.getHospital() != null ? req.getHospital().getHospitalName() : "Hospital");
                        requestContext.put("hospitalAddress", req.getHospital() != null ? req.getHospital().getAddress() : "");
                        requestContext.put("distanceKm", m.getDistanceKm());
                        requestContext.put("myResponseStatus", m.getStatus() != null ? m.getStatus().name() : "");
                        requestContext.put("confirmedByHospital", Boolean.TRUE.equals(m.getConfirmed()));
                        requestContext.put("requestStatus", req.getStatus() != null ? req.getStatus().name() : "");
                    }
                }
            }

        } else if (role == Role.HOSPITAL) {
            Optional<Hospital> hospOpt = hospitalRepository.findByUserId(user.getId());
            if (hospOpt.isPresent()) {
                Hospital hospital = hospOpt.get();
                roleDetails.put("hospitalName", hospital.getHospitalName());
                roleDetails.put("city", hospital.getCity());
                roleDetails.put("verificationStatus", hospital.getVerificationStatus());
                roleDetails.put("verified", Boolean.TRUE.equals(hospital.getVerified()));
                roleDetails.put("status", hospital.getStatus());

                if (requestId != null) {
                    Optional<BloodRequest> reqOpt = bloodRequestRepository.findById(requestId);
                    if (reqOpt.isPresent() && reqOpt.get().getHospital() != null && reqOpt.get().getHospital().getId().equals(hospital.getId())) {
                        BloodRequest req = reqOpt.get();
                        requestContext.put("requestId", req.getId());
                        requestContext.put("bloodGroupNeeded", req.getBloodGroupNeeded() != null ? req.getBloodGroupNeeded().name() : "");
                        requestContext.put("unitsRequired", req.getUnitsRequired());
                        requestContext.put("urgencyLevel", req.getUrgencyLevel() != null ? req.getUrgencyLevel().name() : "");
                        requestContext.put("requestStatus", req.getStatus() != null ? req.getStatus().name() : "");
                        requestContext.put("requiredByDate", req.getRequiredByDate() != null ? req.getRequiredByDate().toString() : "");

                        List<MatchedEmergencyDonor> matches = matchedEmergencyDonorRepository.findByBloodRequestId(req.getId());
                        requestContext.put("totalMatchedDonors", matches.size());
                        requestContext.put("acceptedDonorsCount", matches.stream().filter(m -> m.getStatus() != null && m.getStatus().name().equals("ACCEPTED")).count());
                        requestContext.put("confirmedDonorsCount", matches.stream().filter(m -> Boolean.TRUE.equals(m.getConfirmed())).count());
                    }
                }
            }

        } else if (role == Role.PATIENT) {
            Optional<PatientProfile> patOpt = patientProfileRepository.findByUserId(user.getId());
            if (patOpt.isPresent()) {
                PatientProfile patient = patOpt.get();
                roleDetails.put("patientCode", patient.getPatientCode());
                roleDetails.put("bloodGroup", patient.getBloodGroup() != null ? patient.getBloodGroup().name() : "N/A");
                roleDetails.put("city", patient.getCity());

                if (requestId != null) {
                    Optional<BloodRequest> reqOpt = bloodRequestRepository.findById(requestId);
                    if (reqOpt.isPresent() && reqOpt.get().getPatient() != null && reqOpt.get().getPatient().getId().equals(patient.getId())) {
                        BloodRequest req = reqOpt.get();
                        requestContext.put("requestId", req.getId());
                        requestContext.put("bloodGroupNeeded", req.getBloodGroupNeeded() != null ? req.getBloodGroupNeeded().name() : "");
                        requestContext.put("unitsRequired", req.getUnitsRequired());
                        requestContext.put("requestStatus", req.getStatus() != null ? req.getStatus().name() : "");
                    }
                }
            }

        } else if (role == Role.ADMIN) {
            roleDetails.put("totalHospitals", hospitalRepository.count());
            roleDetails.put("totalBloodRequests", bloodRequestRepository.count());

            if (requestId != null) {
                Optional<BloodRequest> reqOpt = bloodRequestRepository.findById(requestId);
                if (reqOpt.isPresent()) {
                    BloodRequest req = reqOpt.get();
                    requestContext.put("requestId", req.getId());
                    requestContext.put("hospitalName", req.getHospital() != null ? req.getHospital().getHospitalName() : "N/A");
                    requestContext.put("bloodGroupNeeded", req.getBloodGroupNeeded() != null ? req.getBloodGroupNeeded().name() : "");
                    requestContext.put("unitsRequired", req.getUnitsRequired());
                    requestContext.put("requestStatus", req.getStatus() != null ? req.getStatus().name() : "");
                }
            }
        }

        return UserAiContextDTO.builder()
                .role(role.name())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .currentPage(currentPage)
                .roleDetails(roleDetails)
                .activeRequestContext(requestContext.isEmpty() ? null : requestContext)
                .build();
    }
}
