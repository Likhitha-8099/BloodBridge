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
 * Data Transfer Object for creating an Emergency Blood Request by a Hospital.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Hospital Emergency Blood Request Payload")
public class HospitalBloodRequestCreate {

    @NotNull(message = "Blood group needed is required")
    @Schema(description = "Required blood group", example = "O_NEGATIVE")
    private BloodGroup bloodGroupNeeded;

    @NotNull(message = "Units required is required")
    @Min(value = 1, message = "Units required must be at least 1")
    @Schema(description = "Total blood units needed", example = "3")
    private Integer unitsRequired;

    @NotNull(message = "Urgency level is required")
    @Schema(description = "Urgency level (CRITICAL, URGENT, NORMAL)", example = "CRITICAL")
    private UrgencyLevel urgencyLevel;

    @NotNull(message = "Required by date is required")
    @FutureOrPresent(message = "Required by date must be today or in the future")
    @Schema(description = "Required date before which units must be delivered", example = "2026-08-02")
    private LocalDate requiredByDate;

    @Schema(description = "Reason or medical diagnosis", example = "Emergency Trauma Unit Surgery")
    private String reason;

    @Schema(description = "Special notes or instructions for donors", example = "Report directly to Room 402 Blood Bank Center")
    private String notes;

    @Schema(description = "Optional associated patient profile ID", example = "5")
    private Long patientId;
}
