package com.bloodbridge.mapper;

import com.bloodbridge.dto.request.CreateHospitalRequest;
import com.bloodbridge.dto.request.UpdateHospitalRequest;
import com.bloodbridge.dto.response.HospitalResponse;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper component for translating between {@link Hospital} entities and DTOs.
 */
@Component
public class HospitalMapper {

    /**
     * Maps a {@link CreateHospitalRequest} to a {@link Hospital} entity.
     *
     * @param request hospital creation payload
     * @param user associated user entity
     * @return unpersisted hospital entity
     */
    public Hospital toEntity(CreateHospitalRequest request, User user) {
        if (request == null) {
            return null;
        }

        return Hospital.builder()
                .user(user)
                .hospitalName(request.getHospitalName())
                .registrationNumber(request.getRegistrationNumber())
                .licenseNumber(request.getLicenseNumber())
                .hospitalType(request.getHospitalType() != null ? request.getHospitalType() : "GENERAL")
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .website(request.getWebsite())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .operatingHours(request.getOperatingHours() != null ? request.getOperatingHours() : "24/7")
                .emergencyAvailable(request.getEmergencyAvailable() != null ? request.getEmergencyAvailable() : true)
                .verified(false)
                .verificationStatus("PENDING")
                .status("ACTIVE")
                .build();
    }

    /**
     * Maps a {@link Hospital} entity to a detailed {@link HospitalResponse}.
     *
     * @param hospital hospital entity
     * @return mapped HospitalResponse DTO
     */
    public HospitalResponse toResponse(Hospital hospital) {
        if (hospital == null) {
            return null;
        }

        User user = hospital.getUser();

        return HospitalResponse.builder()
                .id(hospital.getId())
                .userId(user != null ? user.getId() : null)
                .hospitalName(hospital.getHospitalName())
                .registrationNumber(hospital.getRegistrationNumber())
                .licenseNumber(hospital.getLicenseNumber())
                .licenseDocumentUrl(hospital.getLicenseDocumentUrl())
                .logoUrl(hospital.getLogoUrl())
                .hospitalType(hospital.getHospitalType())
                .contactPerson(hospital.getContactPerson())
                .email(hospital.getEmail())
                .phoneNumber(hospital.getPhoneNumber())
                .website(hospital.getWebsite())
                .address(hospital.getAddress())
                .city(hospital.getCity())
                .state(hospital.getState())
                .country(hospital.getCountry())
                .postalCode(hospital.getPostalCode())
                .latitude(hospital.getLatitude())
                .longitude(hospital.getLongitude())
                .verified(hospital.getVerified())
                .verificationStatus(hospital.getVerificationStatus())
                .verifiedBy(hospital.getVerifiedBy())
                .verifiedAt(hospital.getVerifiedAt())
                .approvedBy(hospital.getApprovedBy())
                .approvedAt(hospital.getApprovedAt())
                .rejectedBy(hospital.getRejectedBy())
                .rejectedAt(hospital.getRejectedAt())
                .rejectionReason(hospital.getRejectionReason())
                .remarks(hospital.getRemarks())
                .emergencyAvailable(hospital.getEmergencyAvailable())
                .operatingHours(hospital.getOperatingHours())
                .status(hospital.getStatus())
                .createdAt(hospital.getCreatedAt())
                .updatedAt(hospital.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing {@link Hospital} entity using data from an {@link UpdateHospitalRequest}.
     *
     * @param request update hospital profile request DTO
     * @param hospital target hospital entity
     */
    public void updateEntityFromRequest(UpdateHospitalRequest request, Hospital hospital) {
        if (request == null || hospital == null) {
            return;
        }

        if (request.getHospitalName() != null && !request.getHospitalName().isBlank()) {
            hospital.setHospitalName(request.getHospitalName());
        }
        if (request.getContactPerson() != null) {
            hospital.setContactPerson(request.getContactPerson());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            hospital.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            hospital.setEmail(request.getEmail());
        }
        if (request.getWebsite() != null) {
            hospital.setWebsite(request.getWebsite());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            hospital.setAddress(request.getAddress());
        }
        if (request.getCity() != null && !request.getCity().isBlank()) {
            hospital.setCity(request.getCity());
        }
        if (request.getState() != null && !request.getState().isBlank()) {
            hospital.setState(request.getState());
        }
        if (request.getCountry() != null) {
            hospital.setCountry(request.getCountry());
        }
        if (request.getPostalCode() != null) {
            hospital.setPostalCode(request.getPostalCode());
        }
        if (request.getLatitude() != null) {
            hospital.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            hospital.setLongitude(request.getLongitude());
        }
        if (request.getOperatingHours() != null) {
            hospital.setOperatingHours(request.getOperatingHours());
        }
        if (request.getEmergencyAvailable() != null) {
            hospital.setEmergencyAvailable(request.getEmergencyAvailable());
        }
    }
}
