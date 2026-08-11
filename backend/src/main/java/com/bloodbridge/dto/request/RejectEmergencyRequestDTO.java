package com.bloodbridge.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload when a donor rejects an emergency blood request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectEmergencyRequestDTO {

    @NotNull(message = "Emergency request ID is required")
    private Long emergencyRequestId;

    private String reason;
}
