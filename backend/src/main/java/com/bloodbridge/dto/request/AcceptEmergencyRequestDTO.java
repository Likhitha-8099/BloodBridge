package com.bloodbridge.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload when a donor accepts an emergency blood request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptEmergencyRequestDTO {

    @NotNull(message = "Emergency request ID is required")
    private Long emergencyRequestId;

    @Min(value = 1, message = "ETA minutes must be at least 1")
    @Builder.Default
    private Integer etaMinutes = 15;

    private String remarks;
}
