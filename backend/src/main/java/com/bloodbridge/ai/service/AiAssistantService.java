package com.bloodbridge.ai.service;

import com.bloodbridge.ai.dto.AiAssistantRequestDTO;
import com.bloodbridge.ai.dto.AiAssistantResponseDTO;
import com.bloodbridge.dto.response.ApiResponse;

/**
 * Service interface managing AI assistant chat interactions.
 */
public interface AiAssistantService {

    /**
     * Processes a user chat message through Groq API and returns a structured AI response.
     *
     * @param userEmail Email of the authenticated user
     * @param request Request containing user chat message
     * @return Standardized ApiResponse containing AI reply or graceful fallback error message
     */
    ApiResponse<AiAssistantResponseDTO> processChat(String userEmail, AiAssistantRequestDTO request);
}
