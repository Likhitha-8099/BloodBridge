package com.bloodbridge.dto;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.UrgencyLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object for creating a blood request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequestCreateRequest {

    @NotNull(message = "Hospital ID is required")
    private Long hospitalId;

    @NotNull(message = "Blood group needed is required")
    private BloodGroup bloodGroupNeeded;

    @NotNull(message = "Units required is required")
    @Min(value = 1, message = "Units required must be greater than 0")
    private Integer unitsRequired;

    @NotNull(message = "Urgency level is required")
    private UrgencyLevel urgencyLevel;

    @NotBlank(message = "Reason for request is required")
    private String reason;

    @NotNull(message = "Required by date is required")
    private LocalDate requiredByDate;
}
