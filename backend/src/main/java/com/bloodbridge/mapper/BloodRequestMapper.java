package com.bloodbridge.mapper;

import com.bloodbridge.dto.BloodRequestCreateRequest;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.BloodRequestSummaryResponse;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.enums.RequestStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper component for translating between {@link BloodRequest} entities and DTOs.
 */
@Component
public class BloodRequestMapper {

    /**
     * Maps a {@link BloodRequestCreateRequest} to a {@link BloodRequest} entity.
     * Sets requestDate to current time and status to PENDING.
     *
     * @param request the create request details
     * @param patient the patient profile requesting blood
     * @param hospital the hospital selected for verification
     * @return the unpersisted BloodRequest entity
     */
    public BloodRequest toEntity(BloodRequestCreateRequest request, PatientProfile patient, Hospital hospital) {
        if (request == null) {
            return null;
        }

        return BloodRequest.builder()
                .patient(patient)
                .hospital(hospital)
                .bloodGroupNeeded(request.getBloodGroupNeeded())
                .unitsRequired(request.getUnitsRequired())
                .urgencyLevel(request.getUrgencyLevel())
                .reason(request.getReason())
                .requestDate(LocalDateTime.now())
                .requiredByDate(request.getRequiredByDate())
                .status(RequestStatus.PENDING) // starts at PENDING status
                .build();
    }

    /**
     * Maps a {@link BloodRequest} entity to a detailed {@link BloodRequestResponse}.
     *
     * @param request the blood request entity
     * @return the mapped response DTO
     */
    public BloodRequestResponse toResponse(BloodRequest request) {
        if (request == null) {
            return null;
        }

        PatientProfile patient = request.getPatient();
        Hospital hospital = request.getHospital();

        return BloodRequestResponse.builder()
                .id(request.getId())
                .patientId(patient != null ? patient.getId() : null)
                .patientName((patient != null && patient.getUser() != null) ? patient.getUser().getFullName() : null)
                .hospitalId(hospital != null ? hospital.getId() : null)
                .hospitalName(hospital != null ? hospital.getHospitalName() : null)
                .bloodGroupNeeded(request.getBloodGroupNeeded())
                .unitsRequired(request.getUnitsRequired())
                .urgencyLevel(request.getUrgencyLevel())
                .reason(request.getReason())
                .requestDate(request.getRequestDate())
                .requiredByDate(request.getRequiredByDate())
                .status(request.getStatus())
                .notes(request.getNotes())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    /**
     * Maps a {@link BloodRequest} entity to a simplified {@link BloodRequestSummaryResponse}.
     *
     * @param request the blood request entity
     * @return the mapped summary response DTO
     */
    public BloodRequestSummaryResponse toSummaryResponse(BloodRequest request) {
        if (request == null) {
            return null;
        }

        PatientProfile patient = request.getPatient();
        Hospital hospital = request.getHospital();

        return BloodRequestSummaryResponse.builder()
                .id(request.getId())
                .patientName((patient != null && patient.getUser() != null) ? patient.getUser().getFullName() : null)
                .patientCity((patient != null && patient.getUser() != null) ? patient.getUser().getCity() : null)
                .patientState((patient != null && patient.getUser() != null) ? patient.getUser().getState() : null)
                .hospitalName(hospital != null ? hospital.getHospitalName() : null)
                .hospitalAddress(hospital != null ? hospital.getAddress() : null)
                .hospitalCity(hospital != null ? hospital.getCity() : null)
                .hospitalState(hospital != null ? hospital.getState() : null)
                .latitude(hospital != null ? hospital.getLatitude() : null)
                .longitude(hospital != null ? hospital.getLongitude() : null)
                .bloodGroupNeeded(request.getBloodGroupNeeded())
                .unitsRequired(request.getUnitsRequired())
                .urgencyLevel(request.getUrgencyLevel())
                .reason(request.getReason())
                .requiredByDate(request.getRequiredByDate())
                .status(request.getStatus())
                .build();
    }
}
