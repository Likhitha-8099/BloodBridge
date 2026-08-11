package com.bloodbridge.ai.controller;

import com.bloodbridge.ai.dto.AiAssistantRequestDTO;
import com.bloodbridge.ai.dto.AiAssistantResponseDTO;
import com.bloodbridge.ai.service.AiAssistantService;
import com.bloodbridge.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller providing isolated AI Assistant capabilities powered by Groq.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai/assistant")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "Endpoints for BloodBridge AI Assistant chat interactions")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    /**
     * Endpoint for authenticated users to chat with BloodBridge AI Assistant.
     *
     * @param request Validated chat message request payload
     * @return ResponseEntity containing standardized ApiResponse payload
     */
    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Chat with BloodBridge AI Assistant", description = "Sends user prompt to Groq API and returns AI response.")
    public ResponseEntity<ApiResponse<AiAssistantResponseDTO>> chat(@Valid @RequestBody AiAssistantRequestDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth != null ? auth.getName() : null;

        ApiResponse<AiAssistantResponseDTO> response = aiAssistantService.processChat(userEmail, request);
        return ResponseEntity.ok(response);
    }
}
