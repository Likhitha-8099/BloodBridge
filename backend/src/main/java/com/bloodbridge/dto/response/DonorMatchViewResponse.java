package com.bloodbridge.dto.response;

import com.bloodbridge.enums.BloodGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object representing a compatible donor match view item for hospitals.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Matched Donor View Response Payload")
public class DonorMatchViewResponse {

    @Schema(description = "Donor profile ID", example = "5")
    private Long donorId;

    @Schema(description = "Donor full name", example = "John Smith")
    private String fullName;

    @Schema(description = "Blood group", example = "O_NEGATIVE")
    private BloodGroup bloodGroup;

    @Schema(description = "Calculated distance in KM", example = "4.2")
    private Double distanceKm;

    @Schema(description = "Eligibility flag", example = "true")
    private Boolean eligible;

    @Schema(description = "Availability flag", example = "true")
    private Boolean available;

    @Schema(description = "Donor engagement score", example = "140")
    private Integer donorScore;

    @Schema(description = "Last donation date", example = "2026-04-10")
    private LocalDate lastDonationDate;
}
