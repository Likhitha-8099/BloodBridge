package com.bloodbridge.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO representing a matched donor and their response status
 * for an emergency blood request viewed by a hospital.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalDonorResponseDTO {

    private Long matchedDonorId;
    private Long donorId;
    private String donorName;
    private String bloodGroup;
    private String email;
    private String phone;
    private String donorStatus;
    private Double distanceKm;
    private String tierGroup;
    private String matchingGroup;
    private String responseStatus;
    private Boolean confirmed;
    private LocalDateTime confirmedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private String fulfillmentStatus;
    private LocalDateTime createdAt;

    @JsonProperty("donorEmail")
    public String getDonorEmail() {
        return email;
    }

    @JsonProperty("donorPhone")
    public String getDonorPhone() {
        return phone;
    }

    public String getMatchingGroup() {
        return matchingGroup != null ? matchingGroup : tierGroup;
    }
}
