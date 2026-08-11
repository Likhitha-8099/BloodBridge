package com.bloodbridge.mapper;

import com.bloodbridge.dto.DonationResponse;
import com.bloodbridge.dto.DonationSummaryResponse;
import com.bloodbridge.entity.Donation;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.PatientProfile;
import com.bloodbridge.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper component for translating donation entities to response DTOs.
 */
@Component
public class DonationMapper {

    /**
     * Maps a {@link Donation} entity to a detailed {@link DonationResponse}.
     *
     * @param donation the donation entity
     * @return the mapped detailed response DTO
     */
    public DonationResponse toResponse(Donation donation) {
        if (donation == null) {
            return null;
        }

        DonorProfile donor = donation.getDonor();
        User donorUser = donor != null ? donor.getUser() : null;

        PatientProfile patient = donation.getPatient();
        User patientUser = patient != null ? patient.getUser() : null;

        Hospital hospital = donation.getHospital();

        com.bloodbridge.enums.BloodGroup bloodGroup = null;
        if (donor != null && donor.getBloodGroup() != null) {
            bloodGroup = donor.getBloodGroup();
        } else if (donation.getBloodRequest() != null) {
            bloodGroup = donation.getBloodRequest().getBloodGroupNeeded();
        }

        boolean certAvail = donation.getStatus() == com.bloodbridge.enums.DonationStatus.COMPLETED;
        String certId = donation.getCertificateId();
        if (certAvail && (certId == null || certId.isBlank())) {
            certId = "CERT-BB-" + (donation.getDonationDate() != null ? donation.getDonationDate().getYear() : 2026) + "-" + String.format("%06d", donation.getId());
        }

        return DonationResponse.builder()
                .id(donation.getId())
                .donorId(donor != null ? donor.getId() : null)
                .donorName(donorUser != null ? donorUser.getFullName() : null)
                .patientId(patient != null ? patient.getId() : null)
                .patientName(patientUser != null ? patientUser.getFullName() : null)
                .requestId(donation.getBloodRequest() != null ? donation.getBloodRequest().getId() : null)
                .hospitalId(hospital != null ? hospital.getId() : null)
                .hospitalName(hospital != null ? hospital.getHospitalName() : null)
                .matchResultId(donation.getMatchResult() != null ? donation.getMatchResult().getId() : null)
                .donationDate(donation.getDonationDate())
                .unitsDonated(donation.getUnitsDonated())
                .remarks(donation.getRemarks())
                .bloodGroup(bloodGroup)
                .certificateId(certId)
                .certificateAvailable(certAvail)
                .status(donation.getStatus())
                .completedAt(donation.getCompletedAt())
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }

    /**
     * Maps a {@link Donation} entity to a simplified {@link DonationSummaryResponse}.
     *
     * @param donation the donation entity
     * @return the mapped summary response DTO
     */
    public DonationSummaryResponse toSummaryResponse(Donation donation) {
        if (donation == null) {
            return null;
        }

        DonorProfile donor = donation.getDonor();
        User donorUser = donor != null ? donor.getUser() : null;

        PatientProfile patient = donation.getPatient();
        User patientUser = patient != null ? patient.getUser() : null;

        Hospital hospital = donation.getHospital();

        com.bloodbridge.enums.BloodGroup bloodGroup = null;
        if (donor != null && donor.getBloodGroup() != null) {
            bloodGroup = donor.getBloodGroup();
        } else if (donation.getBloodRequest() != null) {
            bloodGroup = donation.getBloodRequest().getBloodGroupNeeded();
        }

        boolean certAvail = donation.getStatus() == com.bloodbridge.enums.DonationStatus.COMPLETED;
        String certId = donation.getCertificateId();
        if (certAvail && (certId == null || certId.isBlank())) {
            certId = "CERT-BB-" + (donation.getDonationDate() != null ? donation.getDonationDate().getYear() : 2026) + "-" + String.format("%06d", donation.getId());
        }

        return DonationSummaryResponse.builder()
                .id(donation.getId())
                .donorName(donorUser != null ? donorUser.getFullName() : null)
                .patientName(patientUser != null ? patientUser.getFullName() : null)
                .hospitalName(hospital != null ? hospital.getHospitalName() : null)
                .bloodGroup(bloodGroup)
                .donationDate(donation.getDonationDate())
                .unitsDonated(donation.getUnitsDonated())
                .status(donation.getStatus())
                .certificateId(certId)
                .certificateAvailable(certAvail)
                .build();
    }
}
