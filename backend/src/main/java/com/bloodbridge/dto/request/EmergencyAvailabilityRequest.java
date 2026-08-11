package com.bloodbridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload for updating Emergency Availability status.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Emergency Availability Request Payload")
public class EmergencyAvailabilityRequest {

    @NotNull(message = "Emergency availability status is required")
    @Schema(description = "Emergency availability flag", example = "true")
    private Boolean emergencyAvailable;
}
