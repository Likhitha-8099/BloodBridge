package com.bloodbridge.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Summary container DTO returning overall statistics and donor responses
 * for an emergency blood request viewed by a hospital.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalEmergencyResponsesContainerDTO {

    private Long requestId;
    private int totalMatchedDonors;
    private int acceptedDonors;
    private int pendingDonors;
    private int rejectedDonors;
    private int confirmedDonors;
    private List<HospitalDonorResponseDTO> responses;

    @JsonProperty("totalMatched")
    public int getTotalMatched() {
        return totalMatchedDonors;
    }

    @JsonProperty("acceptedCount")
    public int getAcceptedCount() {
        return acceptedDonors;
    }

    @JsonProperty("pendingCount")
    public int getPendingCount() {
        return pendingDonors;
    }

    @JsonProperty("rejectedCount")
    public int getRejectedCount() {
        return rejectedDonors;
    }

    @JsonProperty("confirmedCount")
    public int getConfirmedCount() {
        return confirmedDonors;
    }
}
