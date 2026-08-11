package com.bloodbridge.dto.request;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.UrgencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object for creating an Emergency Blood Request by a Patient.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Patient Emergency Blood Request Creation Payload")
public class CreateBloodRequestRequest {

    @NotNull(message = "Blood group needed is required")
    @Schema(description = "Required blood group", example = "A_POSITIVE")
    private BloodGroup bloodGroupNeeded;

    @NotNull(message = "Units required is required")
    @Min(value = 1, message = "Units required must be at least 1")
    @Schema(description = "Number of blood units needed", example = "2")
    private Integer unitsRequired;

    @NotNull(message = "Hospital ID is required")
    @Schema(description = "Selected hospital ID for verification", example = "1")
    private Long hospitalId;

    @NotNull(message = "Required by date is required")
    @FutureOrPresent(message = "Required by date must be today or in the future")
    @Schema(description = "Required by date", example = "2026-08-02")
    private LocalDate requiredByDate;

    @NotNull(message = "Urgency level is required")
    @Schema(description = "Urgency level (CRITICAL, URGENT, NORMAL)", example = "URGENT")
    private UrgencyLevel urgencyLevel;

    @Schema(description = "Patient current medical condition or reason", example = "Scheduled Major Heart Surgery")
    private String reason;

    @Schema(description = "Patient current medical condition or reason", example = "Scheduled Major Heart Surgery")
    private String patientCondition;

    @Schema(description = "Doctor recommendation or prescription notes", example = "Recommended by Dr. Michael Chen")
    private String doctorRecommendation;

    @Schema(description = "Additional notes for donors/hospital", example = "Contact emergency contact if unavailable")
    private String notes;
}
