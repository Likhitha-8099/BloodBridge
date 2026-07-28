package com.bloodbridge.mapper;

import com.bloodbridge.dto.HospitalRequest;
import com.bloodbridge.dto.HospitalResponse;
import com.bloodbridge.dto.HospitalSummaryResponse;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper component for translating between {@link Hospital} entities and their corresponding DTOs.
 */
@Component
public class HospitalMapper {

    /**
     * Maps a {@link HospitalRequest} to a {@link Hospital} entity.
     *
     * @param request the profile request details
     * @param user    the associated user context
     * @return the unpersisted hospital entity
     */
    public Hospital toEntity(HospitalRequest request, User user) {
        if (request == null) {
            return null;
        }

        return Hospital.builder()
                .user(user)
                .hospitalName(request.getHospitalName())
                .registrationNumber(request.getRegistrationNumber())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .verified(false) // default verified is false upon profile creation
                .build();
    }

    /**
     * Maps a {@link Hospital} entity to a detailed {@link HospitalResponse}.
     *
     * @param hospital the hospital entity
     * @return the mapped response DTO
     */
    public HospitalResponse toResponse(Hospital hospital) {
        if (hospital == null) {
            return null;
        }

        User user = hospital.getUser();

        return HospitalResponse.builder()
                .id(hospital.getId())
                .fullName(user != null ? user.getFullName() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .hospitalName(hospital.getHospitalName())
                .registrationNumber(hospital.getRegistrationNumber())
                .email(hospital.getEmail())
                .phoneNumber(hospital.getPhoneNumber())
                .address(hospital.getAddress())
                .city(hospital.getCity())
                .state(hospital.getState())
                .verified(hospital.getVerified())
                .createdAt(hospital.getCreatedAt())
                .updatedAt(hospital.getUpdatedAt())
                .build();
    }

    /**
     * Maps a {@link Hospital} entity to a simplified {@link HospitalSummaryResponse}.
     *
     * @param hospital the hospital entity
     * @return the mapped summary DTO
     */
    public HospitalSummaryResponse toSummaryResponse(Hospital hospital) {
        if (hospital == null) {
            return null;
        }

        return HospitalSummaryResponse.builder()
                .id(hospital.getId())
                .hospitalName(hospital.getHospitalName())
                .registrationNumber(hospital.getRegistrationNumber())
                .email(hospital.getEmail())
                .phoneNumber(hospital.getPhoneNumber())
                .city(hospital.getCity())
                .state(hospital.getState())
                .verified(hospital.getVerified())
                .build();
    }
}
