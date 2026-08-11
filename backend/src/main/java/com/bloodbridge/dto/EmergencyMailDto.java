package com.bloodbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object containing details required for emergency blood alert emails.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyMailDto {

    private Long requestId;
    private Long donorId;
    private Double distanceKm;
    private String toEmail;
    private String donorName;
    private String hospitalName;
    private String bloodGroup;
    private Integer unitsRequired;
    private String urgencyLevel;
    private String hospitalAddress;
    private String city;
    private String state;
    private String requiredByDate;
    private String reason;
    private String loginUrl;
}
