package com.bloodbridge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for creating or initiating a donation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationCreateRequest {

    @NotNull(message = "Match result ID is required")
    private Long matchResultId;

    @Min(value = 1, message = "Units donated must be greater than 0")
    private Integer unitsDonated;

    private String remarks;
}
