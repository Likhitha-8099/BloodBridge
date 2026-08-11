package com.bloodbridge.dto.request;

import com.bloodbridge.enums.UrgencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object for updating an existing Blood Request by a Patient.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Patient Blood Request Update Payload")
public class UpdateBloodRequestRequest {

    @Min(value = 1, message = "Units required must be at least 1")
    @Schema(description = "Updated units required", example = "3")
    private Integer unitsRequired;

    @FutureOrPresent(message = "Required by date must be today or in the future")
    @Schema(description = "Updated required by date", example = "2026-08-03")
    private LocalDate requiredByDate;

    @Schema(description = "Updated urgency level", example = "CRITICAL")
    private UrgencyLevel urgencyLevel;

    @Schema(description = "Updated patient condition details", example = "Emergency surgery rescheduled to early morning")
    private String patientCondition;

    @Schema(description = "Updated notes", example = "Urgent: Direct hospital delivery requested")
    private String notes;
}
