package com.bloodbridge.dto.response;

import com.bloodbridge.enums.EligibilityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object representing Smart Donor Eligibility details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Smart Donor Eligibility Response Payload")
public class EligibilityResponse {

    @Schema(description = "Calculated eligibility status", example = "ELIGIBLE")
    private EligibilityStatus status;

    @Schema(description = "Boolean flag indicating if donor is eligible to donate today", example = "true")
    private boolean eligible;

    @Schema(description = "Date when donor becomes eligible for next donation", example = "2026-08-01")
    private LocalDate nextEligibleDate;

    @Schema(description = "Days remaining until eligible", example = "0")
    private long daysUntilEligible;

    @Schema(description = "Detailed reason explaining eligibility or deferral", example = "Donor meets all medical age, weight, and donation interval criteria.")
    private String reason;

    @Schema(description = "Actionable health or donation recommendation", example = "Stay hydrated and maintain iron-rich diet before your next scheduled donation.")
    private String recommendation;
}
