package com.bloodbridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload for updating preferred donation radius.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Preferred Radius Request Payload")
public class PreferredRadiusRequest {

    @NotNull(message = "Preferred donation radius is required")
    @Min(value = 1, message = "Radius must be at least 1 KM")
    @Schema(description = "Preferred donation radius in KM", example = "25.0")
    private Double preferredDonationRadius;
}
