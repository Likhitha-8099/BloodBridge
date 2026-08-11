package com.bloodbridge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload containing the AI Assistant reply.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAssistantResponseDTO {

    private String reply;
}
