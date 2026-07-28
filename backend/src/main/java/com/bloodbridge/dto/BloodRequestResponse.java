package com.bloodbridge.dto;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.enums.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing the detailed response of a blood request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequestResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long hospitalId;
    private String hospitalName;
    private BloodGroup bloodGroupNeeded;
    private Integer unitsRequired;
    private UrgencyLevel urgencyLevel;
    private String reason;
    private LocalDateTime requestDate;
    private LocalDate requiredByDate;
    private RequestStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
