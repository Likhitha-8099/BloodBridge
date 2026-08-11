package com.bloodbridge.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for the BloodBridge AI Assistant endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAssistantRequestDTO {

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 2000, message = "Message exceeds maximum allowed length of 2000 characters")
    private String message;

    private String currentPage;
    private Long requestId;

    public AiAssistantRequestDTO(String message) {
        this.message = message;
    }
}
