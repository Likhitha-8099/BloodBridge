package com.bloodbridge.service;

import com.bloodbridge.dto.request.AcceptEmergencyRequestDTO;
import com.bloodbridge.dto.request.RejectEmergencyRequestDTO;
import com.bloodbridge.dto.response.EmergencyResponseDTO;
import com.bloodbridge.dto.response.HospitalEmergencyLiveStatsDTO;

import java.util.List;

/**
 * Service interface for handling real-time donor responses & journey tracking for emergency blood requests.
 */
public interface EmergencyResponseService {

    EmergencyResponseDTO acceptEmergencyRequest(String donorEmail, AcceptEmergencyRequestDTO dto);

    EmergencyResponseDTO rejectEmergencyRequest(String donorEmail, RejectEmergencyRequestDTO dto);

    List<EmergencyResponseDTO> getMyResponses(String donorEmail);

    HospitalEmergencyLiveStatsDTO getHospitalLiveStats(Long requestId);

    // Journey Tracking Methods
    EmergencyResponseDTO startTravel(String donorEmail, Long emergencyRequestId);

    EmergencyResponseDTO reachHospital(String donorEmail, Long emergencyRequestId);

    EmergencyResponseDTO completeDonation(String donorEmail, Long emergencyRequestId);

    // Auto-Cancellation Method
    void cancelEmergencyByHospital(Long emergencyRequestId, String hospitalEmail);
}
