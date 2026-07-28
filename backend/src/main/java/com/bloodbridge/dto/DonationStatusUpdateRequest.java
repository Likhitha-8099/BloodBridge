package com.bloodbridge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object containing parameters for final donation execution (completion/status updates).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationStatusUpdateRequest {

    @NotNull(message = "Units donated is required")
    @Min(value = 1, message = "Units donated must be greater than 0")
    private Integer unitsDonated;

    private String remarks;
}
