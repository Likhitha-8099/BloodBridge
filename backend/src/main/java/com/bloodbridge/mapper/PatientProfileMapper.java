package com.bloodbridge.mapper;

import com.bloodbridge.dto.request.CreatePatientProfileRequest;
import com.bloodbridge.dto.request.UpdatePatientProfileRequest;
import com.bloodbridge.dto.response.PatientProfileResponse;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper component for translating between {@link PatientProfile} entities and DTOs.
 */
@Component
public class PatientProfileMapper {

    /**
     * Maps a {@link CreatePatientProfileRequest} to a {@link PatientProfile} entity.
     *
     * @param request creation payload
     * @param user user entity
     * @param hospital assigned hospital entity (optional)
     * @return unpersisted PatientProfile entity
     */
    public PatientProfile toEntity(CreatePatientProfileRequest request, User user, Hospital hospital) {
        if (request == null) {
            return null;
        }

        return PatientProfile.builder()
                .user(user)
                .patientCode("PAT-" + System.currentTimeMillis() % 100000)
                .age(request.getAge())
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .rhFactor(request.getRhFactor() != null ? request.getRhFactor() : "POSITIVE")
                .weight(request.getWeight())
                .medicalCondition(request.getMedicalCondition())
                .diagnosis(request.getDiagnosis())
                .doctorName(request.getDoctorName())
                .hospital(hospital)
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactNumber(request.getEmergencyContactNumber())
                .relationship(request.getRelationship())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .preferredHospital(request.getPreferredHospital())
                .medicalHistory(request.getMedicalHistory())
                .status("ACTIVE")
                .build();
    }

    /**
     * Maps a {@link PatientProfile} entity to a detailed {@link PatientProfileResponse}.
     *
     * @param profile patient profile entity
     * @return mapped PatientProfileResponse DTO
     */
    public PatientProfileResponse toResponse(PatientProfile profile) {
        if (profile == null) {
            return null;
        }

        User user = profile.getUser();
        Hospital hospital = profile.getHospital();

        return PatientProfileResponse.builder()
                .id(profile.getId())
                .userId(user != null ? user.getId() : null)
                .fullName(user != null ? user.getFullName() : null)
                .email(user != null ? user.getEmail() : null)
                .phoneNumber(user != null ? user.getPhoneNumber() : null)
                .patientCode(profile.getPatientCode())
                .age(profile.getAge())
                .gender(profile.getGender())
                .bloodGroup(profile.getBloodGroup())
                .rhFactor(profile.getRhFactor())
                .weight(profile.getWeight())
                .medicalCondition(profile.getMedicalCondition())
                .diagnosis(profile.getDiagnosis())
                .doctorName(profile.getDoctorName())
                .hospitalId(hospital != null ? hospital.getId() : null)
                .hospitalName(hospital != null ? hospital.getHospitalName() : null)
                .emergencyContactName(profile.getEmergencyContactName())
                .emergencyContactNumber(profile.getEmergencyContactNumber())
                .relationship(profile.getRelationship())
                .address(profile.getAddress())
                .city(profile.getCity())
                .state(profile.getState())
                .country(profile.getCountry())
                .postalCode(profile.getPostalCode())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .preferredHospital(profile.getPreferredHospital())
                .medicalHistory(profile.getMedicalHistory())
                .status(profile.getStatus())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing {@link PatientProfile} entity using data from an {@link UpdatePatientProfileRequest}.
     *
     * @param request update payload
     * @param profile target patient entity
     * @param hospital assigned hospital entity (optional)
     */
    public void updateEntityFromRequest(UpdatePatientProfileRequest request, PatientProfile profile, Hospital hospital) {
        if (request == null || profile == null) {
            return;
        }

        if (request.getAge() != null) {
            profile.setAge(request.getAge());
        }
        if (request.getWeight() != null) {
            profile.setWeight(request.getWeight());
        }
        if (request.getMedicalCondition() != null) {
            profile.setMedicalCondition(request.getMedicalCondition());
        }
        if (request.getDiagnosis() != null) {
            profile.setDiagnosis(request.getDiagnosis());
        }
        if (request.getDoctorName() != null) {
            profile.setDoctorName(request.getDoctorName());
        }
        if (hospital != null) {
            profile.setHospital(hospital);
        }
        if (request.getEmergencyContactName() != null && !request.getEmergencyContactName().isBlank()) {
            profile.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactNumber() != null && !request.getEmergencyContactNumber().isBlank()) {
            profile.setEmergencyContactNumber(request.getEmergencyContactNumber());
        }
        if (request.getRelationship() != null) {
            profile.setRelationship(request.getRelationship());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getCity() != null && !request.getCity().isBlank()) {
            profile.setCity(request.getCity());
        }
        if (request.getState() != null && !request.getState().isBlank()) {
            profile.setState(request.getState());
        }
        if (request.getCountry() != null) {
            profile.setCountry(request.getCountry());
        }
        if (request.getPostalCode() != null) {
            profile.setPostalCode(request.getPostalCode());
        }
        if (request.getLatitude() != null) {
            profile.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            profile.setLongitude(request.getLongitude());
        }
        if (request.getPreferredHospital() != null) {
            profile.setPreferredHospital(request.getPreferredHospital());
        }
        if (request.getMedicalHistory() != null) {
            profile.setMedicalHistory(request.getMedicalHistory());
        }
    }
}
