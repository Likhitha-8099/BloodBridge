package com.bloodbridge.entity;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a donor profile in the Blood Bridge system.
 */
@Entity
@Table(name = "donor_profiles", indexes = {
        @Index(name = "idx_donor_blood_group", columnList = "blood_group"),
        @Index(name = "idx_donor_city", columnList = "city"),
        @Index(name = "idx_donor_state", columnList = "state"),
        @Index(name = "idx_donor_lat_lon", columnList = "latitude, longitude"),
        @Index(name = "idx_donor_available", columnList = "available_for_donation"),
        @Index(name = "idx_donor_email", columnList = "email")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class DonorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull(message = "User reference is required")
    private User user;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @Column(name = "rh_factor")
    private String rhFactor;

    @Column(name = "age", nullable = false)
    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 65, message = "Age cannot exceed 65")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    @NotNull(message = "Gender is required")
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @NotBlank(message = "City is required")
    @Column(name = "city", nullable = false)
    private String city;

    @NotBlank(message = "State is required")
    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "weight", nullable = false)
    @NotNull(message = "Weight is required")
    @Min(value = 50, message = "Weight must be at least 50 kg")
    private Double weight;

    @Column(name = "height")
    private Double height;

    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;

    @Column(name = "next_eligible_date")
    private LocalDate nextEligibleDate;

    @Builder.Default
    @Column(name = "available_for_donation", nullable = false)
    private Boolean availableForDonation = true;

    @Builder.Default
    @Column(name = "emergency_available", nullable = false)
    private Boolean emergencyAvailable = true;

    @Builder.Default
    @Column(name = "preferred_donation_radius", nullable = false)
    private Double preferredDonationRadius = 25.0;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "medical_conditions", length = 1000)
    private String medicalConditions;

    @Column(name = "current_medications", length = 1000)
    private String currentMedications;

    // --- NEW HEALTHCARE, LIFESTYLE & PREFERENCE FIELDS ---
    @Column(name = "alternate_phone_number")
    private String alternatePhoneNumber;

    @Column(name = "aadhaar_number")
    private String aadhaarNumber;

    @Column(name = "govt_id_type")
    private String govtIdType;

    @Column(name = "govt_id_number")
    private String govtIdNumber;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_number")
    private String emergencyContactNumber;

    @Column(name = "emergency_contact_relationship")
    private String emergencyContactRelationship;

    @Column(name = "country")
    private String country;

    @Column(name = "district")
    private String district;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "address", length = 1000)
    private String address;

    @Column(name = "landmark")
    private String landmark;

    @Column(name = "bmi")
    private Double bmi;

    @Column(name = "hemoglobin")
    private Double hemoglobin;

    @Column(name = "blood_pressure")
    private String bloodPressure;

    @Column(name = "pulse_rate")
    private Integer pulseRate;

    @Builder.Default
    @Column(name = "smoking")
    private Boolean smoking = false;

    @Builder.Default
    @Column(name = "alcohol")
    private Boolean alcohol = false;

    @Builder.Default
    @Column(name = "drug_usage")
    private Boolean drugUsage = false;

    @Builder.Default
    @Column(name = "pregnancy")
    private Boolean pregnancy = false;

    @Builder.Default
    @Column(name = "breastfeeding")
    private Boolean breastfeeding = false;

    @Builder.Default
    @Column(name = "recent_surgery")
    private Boolean recentSurgery = false;

    @Builder.Default
    @Column(name = "recent_tattoo")
    private Boolean recentTattoo = false;

    @Builder.Default
    @Column(name = "recent_vaccination")
    private Boolean recentVaccination = false;

    @Builder.Default
    @Column(name = "recent_fever")
    private Boolean recentFever = false;

    @Column(name = "allergies", length = 1000)
    private String allergies;

    @Column(name = "covid_history")
    private String covidHistory;

    @Column(name = "travel_history")
    private String travelHistory;

    @Column(name = "preferred_hospitals", length = 1000)
    private String preferredHospitals;

    @Builder.Default
    @Column(name = "preferred_contact_method")
    private String preferredContactMethod = "EMAIL";

    @Column(name = "available_days")
    private String availableDays;

    @Column(name = "available_time_slots")
    private String availableTimeSlots;

    @Builder.Default
    @Column(name = "willing_donate_platelets")
    private Boolean willingDonatePlatelets = true;

    @Builder.Default
    @Column(name = "willing_donate_plasma")
    private Boolean willingDonatePlasma = true;

    @Builder.Default
    @Column(name = "rare_blood_donor")
    private Boolean rareBloodDonor = false;

    @Builder.Default
    @Column(name = "push_notification_enabled")
    private Boolean pushNotificationEnabled = true;

    @Builder.Default
    @Column(name = "total_donations", nullable = false)
    private Integer totalDonations = 0;

    @Builder.Default
    @Column(name = "lives_saved", nullable = false)
    private Integer livesSaved = 0;

    @Builder.Default
    @Column(name = "donor_score", nullable = false)
    private Integer donorScore = 100;

    @Builder.Default
    @Column(name = "verification_status", nullable = false)
    private String verificationStatus = "VERIFIED";

    @Builder.Default
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public BloodGroup getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(BloodGroup bloodGroup) { this.bloodGroup = bloodGroup; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(LocalDate lastDonationDate) { this.lastDonationDate = lastDonationDate; }
    public Boolean getAvailableForDonation() { return availableForDonation != null ? availableForDonation : true; }
    public Boolean getIsAvailableForDonation() { return getAvailableForDonation(); }
    public void setAvailableForDonation(Boolean availableForDonation) { this.availableForDonation = availableForDonation; }
    public void setIsAvailableForDonation(Boolean availableForDonation) { setAvailableForDonation(availableForDonation); }
    public Boolean getEmergencyAvailable() { return emergencyAvailable != null ? emergencyAvailable : true; }
    public void setEmergencyAvailable(Boolean emergencyAvailable) { this.emergencyAvailable = emergencyAvailable; }
    public Double getPreferredDonationRadius() { return preferredDonationRadius != null ? preferredDonationRadius : 25.0; }
    public void setPreferredDonationRadius(Double preferredDonationRadius) { this.preferredDonationRadius = preferredDonationRadius; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(String medicalConditions) { this.medicalConditions = medicalConditions; }
    public Integer getDonorScore() { return donorScore != null ? donorScore : 100; }
    public void setDonorScore(Integer donorScore) { this.donorScore = donorScore; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
    public Integer getTotalDonations() { return totalDonations != null ? totalDonations : 0; }
    public void setTotalDonations(Integer totalDonations) { this.totalDonations = totalDonations; }
    public Integer getLivesSaved() { return livesSaved != null ? livesSaved : 0; }
    public void setLivesSaved(Integer livesSaved) { this.livesSaved = livesSaved; }
}
