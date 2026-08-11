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

/**
 * Data Transfer Object representing a summary of a blood request.
 * Used in searches and general list endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequestSummaryResponse {

    private Long id;
    private String patientName;
    private String patientCity;
    private String patientState;
    private String hospitalName;
    private String hospitalAddress;
    private String hospitalCity;
    private String hospitalState;
    private Double latitude;
    private Double longitude;
    private BloodGroup bloodGroupNeeded;
    private Integer unitsRequired;
    private UrgencyLevel urgencyLevel;
    private String reason;
    private Double distanceKm;
    private LocalDate requiredByDate;
    private RequestStatus status;
}
