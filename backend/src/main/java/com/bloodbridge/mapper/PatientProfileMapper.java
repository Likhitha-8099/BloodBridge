package com.bloodbridge.mapper;

import com.bloodbridge.dto.PatientProfileRequest;
import com.bloodbridge.dto.PatientProfileResponse;
import com.bloodbridge.dto.PatientSummaryResponse;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper component for translating between {@link PatientProfile} entities and their corresponding DTOs.
 */
@Component
public class PatientProfileMapper {

    /**
     * Maps a {@link PatientProfileRequest} to a {@link PatientProfile} entity.
     *
     * @param request the profile request payload
     * @param user    the associated user entity
     * @return the unpersisted patient profile entity
     */
    public PatientProfile toEntity(PatientProfileRequest request, User user) {
        if (request == null) {
            return null;
        }

        return PatientProfile.builder()
                .user(user)
                .age(request.getAge())
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactNumber(request.getEmergencyContactNumber())
                .medicalHistory(request.getMedicalHistory())
                .build();
    }

    /**
     * Maps a {@link PatientProfile} entity to a detailed {@link PatientProfileResponse}.
     *
     * @param profile the patient profile entity
     * @return the mapped profile response DTO
     */
    public PatientProfileResponse toResponse(PatientProfile profile) {
        if (profile == null) {
            return null;
        }

        User user = profile.getUser();

        return PatientProfileResponse.builder()
                .id(profile.getId())
                .fullName(user != null ? user.getFullName() : null)
                .email(user != null ? user.getEmail() : null)
                .phoneNumber(user != null ? user.getPhoneNumber() : null)
                .age(profile.getAge())
                .gender(profile.getGender())
                .bloodGroup(profile.getBloodGroup())
                .address(profile.getAddress())
                .city(profile.getCity())
                .state(profile.getState())
                .emergencyContactName(profile.getEmergencyContactName())
                .emergencyContactNumber(profile.getEmergencyContactNumber())
                .medicalHistory(profile.getMedicalHistory())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    /**
     * Maps a {@link PatientProfile} entity to a simplified {@link PatientSummaryResponse}.
     *
     * @param profile the patient profile entity
     * @return the mapped summary response DTO
     */
    public PatientSummaryResponse toSummaryResponse(PatientProfile profile) {
        if (profile == null) {
            return null;
        }

        User user = profile.getUser();

        return PatientSummaryResponse.builder()
                .id(profile.getId())
                .fullName(user != null ? user.getFullName() : null)
                .email(user != null ? user.getEmail() : null)
                .phoneNumber(user != null ? user.getPhoneNumber() : null)
                .age(profile.getAge())
                .gender(profile.getGender())
                .bloodGroup(profile.getBloodGroup())
                .city(profile.getCity())
                .state(profile.getState())
                .emergencyContactName(profile.getEmergencyContactName())
                .emergencyContactNumber(profile.getEmergencyContactNumber())
                .build();
    }
}
