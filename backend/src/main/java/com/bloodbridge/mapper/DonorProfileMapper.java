package com.bloodbridge.mapper;

import com.bloodbridge.dto.request.CreateDonorProfileRequest;
import com.bloodbridge.dto.request.UpdateDonorProfileRequest;
import com.bloodbridge.dto.response.DonorProfileResponse;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.EligibilityStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Mapper component for translating between {@link DonorProfile} entities and DTOs.
 */
@Component
public class DonorProfileMapper {

    /**
     * Maps a {@link CreateDonorProfileRequest} to a {@link DonorProfile} entity.
     *
     * @param request profile creation payload
     * @param user associated user entity
     * @return unpersisted donor profile entity
     */
    public DonorProfile toEntity(CreateDonorProfileRequest request, User user) {
        if (request == null) {
            return null;
        }

        return DonorProfile.builder()
                .user(user)
                .email(request.getEmail() != null ? request.getEmail() : (user != null ? user.getEmail() : null))
                .bloodGroup(request.getBloodGroup())
                .rhFactor(request.getBloodGroup() != null && request.getBloodGroup().name().contains("POSITIVE") ? "POSITIVE" : "NEGATIVE")
                .age(request.getAge())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .city(request.getCity())
                .state(request.getState())
                .weight(request.getWeight())
                .height(request.getHeight())
                .medicalConditions(request.getMedicalConditions())
                .currentMedications(request.getCurrentMedications())
                .preferredDonationRadius(request.getPreferredDonationRadius() != null ? request.getPreferredDonationRadius() : 25.0)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .availableForDonation(true)
                .emergencyAvailable(request.getEmergencyAvailable() != null ? request.getEmergencyAvailable() : true)
                .totalDonations(0)
                .livesSaved(0)
                .donorScore(100)
                .verificationStatus("VERIFIED")
                .status("ACTIVE")
                .build();
    }

    /**
     * Maps a {@link DonorProfile} entity to a detailed {@link DonorProfileResponse}.
     *
     * @param profile donor profile entity
     * @return mapped DonorProfileResponse DTO
     */
    public DonorProfileResponse toResponse(DonorProfile profile) {
        return toResponse(profile, com.bloodbridge.enums.EligibilityStatus.ELIGIBLE, profile != null ? profile.getNextEligibleDate() : null);
    }

    /**
     * Maps a {@link DonorProfile} entity to a detailed {@link DonorProfileResponse}.
     *
     * @param profile donor profile entity
     * @param eligibilityStatus calculated eligibility status
     * @param nextEligibleDate calculated next eligible date
     * @return mapped DonorProfileResponse DTO
     */
    public DonorProfileResponse toResponse(DonorProfile profile, EligibilityStatus eligibilityStatus, LocalDate nextEligibleDate) {
        return toResponse(profile, eligibilityStatus, nextEligibleDate, eligibilityStatus == EligibilityStatus.ELIGIBLE, 0L, 90);
    }

    public DonorProfileResponse toResponse(DonorProfile profile, EligibilityStatus eligibilityStatus, LocalDate nextEligibleDate, Boolean eligible, Long daysUntilEligible, Integer cooldownDays) {
        if (profile == null) {
            return null;
        }

        User user = profile.getUser();

        java.util.List<String> missing = new java.util.ArrayList<>();
        int total = 15;
        int completed = 0;

        if (profile.getBloodGroup() != null) completed++; else missing.add("Blood Group");
        if (profile.getAge() != null) completed++; else missing.add("Age");
        if (profile.getGender() != null) completed++; else missing.add("Gender");
        if (profile.getCity() != null && !profile.getCity().isBlank()) completed++; else missing.add("City");
        if (profile.getState() != null && !profile.getState().isBlank()) completed++; else missing.add("State");
        if (profile.getWeight() != null) completed++; else missing.add("Weight");
        if (profile.getHeight() != null) completed++; else missing.add("Height");
        if (profile.getEmergencyContactName() != null && !profile.getEmergencyContactName().isBlank()) completed++; else missing.add("Emergency Contact");
        if (profile.getPostalCode() != null && !profile.getPostalCode().isBlank()) completed++; else missing.add("PIN Code");
        if (profile.getAddress() != null && !profile.getAddress().isBlank()) completed++; else missing.add("Full Address");
        if (profile.getHemoglobin() != null) completed++; else missing.add("Hemoglobin");
        if (profile.getBloodPressure() != null && !profile.getBloodPressure().isBlank()) completed++; else missing.add("Blood Pressure");
        if (profile.getGovtIdType() != null && !profile.getGovtIdType().isBlank()) completed++; else missing.add("Govt ID");
        if (profile.getOccupation() != null && !profile.getOccupation().isBlank()) completed++; else missing.add("Occupation");
        if (profile.getPreferredContactMethod() != null) completed++; else missing.add("Preferred Contact Method");

        int completionPct = (completed * 100) / total;

        return DonorProfileResponse.builder()
                .id(profile.getId())
                .userId(user != null ? user.getId() : null)
                .fullName(user != null ? user.getFullName() : null)
                .email(profile.getEmail() != null ? profile.getEmail() : (user != null ? user.getEmail() : null))
                .phoneNumber(user != null ? user.getPhoneNumber() : null)
                .bloodGroup(profile.getBloodGroup())
                .rhFactor(profile.getRhFactor())
                .age(profile.getAge())
                .gender(profile.getGender())
                .dateOfBirth(profile.getDateOfBirth())
                .city(profile.getCity())
                .state(profile.getState())
                .weight(profile.getWeight())
                .height(profile.getHeight())
                .lastDonationDate(profile.getLastDonationDate())
                .nextEligibleDate(nextEligibleDate != null ? nextEligibleDate : profile.getNextEligibleDate())
                .availableForDonation(profile.getAvailableForDonation())
                .emergencyAvailable(profile.getEmergencyAvailable())
                .preferredDonationRadius(profile.getPreferredDonationRadius())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .medicalConditions(profile.getMedicalConditions())
                .currentMedications(profile.getCurrentMedications())
                .alternatePhoneNumber(profile.getAlternatePhoneNumber())
                .aadhaarNumber(profile.getAadhaarNumber())
                .govtIdType(profile.getGovtIdType())
                .govtIdNumber(profile.getGovtIdNumber())
                .occupation(profile.getOccupation())
                .emergencyContactName(profile.getEmergencyContactName())
                .emergencyContactNumber(profile.getEmergencyContactNumber())
                .emergencyContactRelationship(profile.getEmergencyContactRelationship())
                .country(profile.getCountry())
                .district(profile.getDistrict())
                .postalCode(profile.getPostalCode())
                .address(profile.getAddress())
                .landmark(profile.getLandmark())
                .bmi(profile.getBmi())
                .hemoglobin(profile.getHemoglobin())
                .bloodPressure(profile.getBloodPressure())
                .pulseRate(profile.getPulseRate())
                .smoking(profile.getSmoking())
                .alcohol(profile.getAlcohol())
                .drugUsage(profile.getDrugUsage())
                .pregnancy(profile.getPregnancy())
                .breastfeeding(profile.getBreastfeeding())
                .recentSurgery(profile.getRecentSurgery())
                .recentTattoo(profile.getRecentTattoo())
                .recentVaccination(profile.getRecentVaccination())
                .recentFever(profile.getRecentFever())
                .allergies(profile.getAllergies())
                .covidHistory(profile.getCovidHistory())
                .travelHistory(profile.getTravelHistory())
                .preferredHospitals(profile.getPreferredHospitals())
                .preferredContactMethod(profile.getPreferredContactMethod())
                .availableDays(profile.getAvailableDays())
                .availableTimeSlots(profile.getAvailableTimeSlots())
                .willingDonatePlatelets(profile.getWillingDonatePlatelets())
                .willingDonatePlasma(profile.getWillingDonatePlasma())
                .rareBloodDonor(profile.getRareBloodDonor())
                .pushNotificationEnabled(profile.getPushNotificationEnabled())
                .profileCompletionPercentage(completionPct)
                .missingFields(missing)
                .totalDonations(profile.getTotalDonations())
                .livesSaved(profile.getLivesSaved())
                .donorScore(profile.getDonorScore())
                .eligibilityStatus(eligibilityStatus)
                .eligible(eligible)
                .daysUntilEligible(daysUntilEligible)
                .cooldownDays(cooldownDays)
                .verificationStatus(profile.getVerificationStatus())
                .status(profile.getStatus())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing {@link DonorProfile} entity using data from an {@link UpdateDonorProfileRequest}.
     */
    public void updateEntityFromRequest(UpdateDonorProfileRequest request, DonorProfile profile) {
        if (request == null || profile == null) {
            return;
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) profile.setEmail(request.getEmail());
        if (request.getBloodGroup() != null) profile.setBloodGroup(request.getBloodGroup());
        if (request.getCity() != null && !request.getCity().isBlank()) profile.setCity(request.getCity());
        if (request.getState() != null && !request.getState().isBlank()) profile.setState(request.getState());
        if (request.getWeight() != null) profile.setWeight(request.getWeight());
        if (request.getHeight() != null) profile.setHeight(request.getHeight());
        if (request.getMedicalConditions() != null) profile.setMedicalConditions(request.getMedicalConditions());
        if (request.getCurrentMedications() != null) profile.setCurrentMedications(request.getCurrentMedications());
        if (request.getLastDonationDate() != null) profile.setLastDonationDate(request.getLastDonationDate());
        if (request.getPreferredDonationRadius() != null) profile.setPreferredDonationRadius(request.getPreferredDonationRadius());
        if (request.getLatitude() != null) profile.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) profile.setLongitude(request.getLongitude());

        if (request.getAlternatePhoneNumber() != null) profile.setAlternatePhoneNumber(request.getAlternatePhoneNumber());
        if (request.getAadhaarNumber() != null) profile.setAadhaarNumber(request.getAadhaarNumber());
        if (request.getGovtIdType() != null) profile.setGovtIdType(request.getGovtIdType());
        if (request.getGovtIdNumber() != null) profile.setGovtIdNumber(request.getGovtIdNumber());
        if (request.getOccupation() != null) profile.setOccupation(request.getOccupation());
        if (request.getEmergencyContactName() != null) profile.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactNumber() != null) profile.setEmergencyContactNumber(request.getEmergencyContactNumber());
        if (request.getEmergencyContactRelationship() != null) profile.setEmergencyContactRelationship(request.getEmergencyContactRelationship());
        if (request.getCountry() != null) profile.setCountry(request.getCountry());
        if (request.getDistrict() != null) profile.setDistrict(request.getDistrict());
        if (request.getPostalCode() != null) profile.setPostalCode(request.getPostalCode());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getLandmark() != null) profile.setLandmark(request.getLandmark());
        if (request.getBmi() != null) profile.setBmi(request.getBmi());
        if (request.getHemoglobin() != null) profile.setHemoglobin(request.getHemoglobin());
        if (request.getBloodPressure() != null) profile.setBloodPressure(request.getBloodPressure());
        if (request.getPulseRate() != null) profile.setPulseRate(request.getPulseRate());
        if (request.getSmoking() != null) profile.setSmoking(request.getSmoking());
        if (request.getAlcohol() != null) profile.setAlcohol(request.getAlcohol());
        if (request.getDrugUsage() != null) profile.setDrugUsage(request.getDrugUsage());
        if (request.getPregnancy() != null) profile.setPregnancy(request.getPregnancy());
        if (request.getBreastfeeding() != null) profile.setBreastfeeding(request.getBreastfeeding());
        if (request.getRecentSurgery() != null) profile.setRecentSurgery(request.getRecentSurgery());
        if (request.getRecentTattoo() != null) profile.setRecentTattoo(request.getRecentTattoo());
        if (request.getRecentVaccination() != null) profile.setRecentVaccination(request.getRecentVaccination());
        if (request.getRecentFever() != null) profile.setRecentFever(request.getRecentFever());
        if (request.getAllergies() != null) profile.setAllergies(request.getAllergies());
        if (request.getCovidHistory() != null) profile.setCovidHistory(request.getCovidHistory());
        if (request.getTravelHistory() != null) profile.setTravelHistory(request.getTravelHistory());
        if (request.getPreferredHospitals() != null) profile.setPreferredHospitals(request.getPreferredHospitals());
        if (request.getPreferredContactMethod() != null) profile.setPreferredContactMethod(request.getPreferredContactMethod());
        if (request.getAvailableDays() != null) profile.setAvailableDays(request.getAvailableDays());
        if (request.getAvailableTimeSlots() != null) profile.setAvailableTimeSlots(request.getAvailableTimeSlots());
        if (request.getWillingDonatePlatelets() != null) profile.setWillingDonatePlatelets(request.getWillingDonatePlatelets());
        if (request.getWillingDonatePlasma() != null) profile.setWillingDonatePlasma(request.getWillingDonatePlasma());
        if (request.getRareBloodDonor() != null) profile.setRareBloodDonor(request.getRareBloodDonor());
        if (request.getPushNotificationEnabled() != null) profile.setPushNotificationEnabled(request.getPushNotificationEnabled());
        if (request.getEmergencyAvailable() != null) profile.setEmergencyAvailable(request.getEmergencyAvailable());
        if (request.getAvailableForDonation() != null) profile.setAvailableForDonation(request.getAvailableForDonation());
    }
}
