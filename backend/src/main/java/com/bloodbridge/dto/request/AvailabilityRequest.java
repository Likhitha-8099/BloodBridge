package com.bloodbridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for toggling Donor Availability for donation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Toggle Donor Availability Request Payload")
public class AvailabilityRequest {

    @NotNull(message = "Availability status is required")
    @Schema(description = "Availability status flag", example = "true")
    private Boolean availableForDonation;
}
