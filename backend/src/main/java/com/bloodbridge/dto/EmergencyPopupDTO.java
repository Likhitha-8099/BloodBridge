package com.bloodbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO sent via WebSocket STOMP to matched donors for real-time emergency popup alerts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyPopupDTO {

    private Long emergencyRequestId;
    private String hospitalName;
    private String hospitalAddress;
    private String city;
    private String state;
    private String bloodGroupNeeded;
    private Integer unitsRequired;
    private String urgencyLevel;
    private Double distanceKm;
    private String reason;
    private String requiredByDate;
    private Double hospitalLatitude;
    private Double hospitalLongitude;
    private String actionUrl;
}
