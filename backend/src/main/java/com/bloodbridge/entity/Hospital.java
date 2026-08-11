package com.bloodbridge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a hospital in the Blood Bridge system.
 */
@Entity
@Table(name = "hospitals")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull(message = "User reference is required")
    private User user;

    @NotBlank(message = "Hospital name is required")
    @Column(name = "hospital_name", nullable = false)
    private String hospitalName;

    @NotBlank(message = "Registration number is required")
    @Column(name = "registration_number", nullable = false, unique = true)
    private String registrationNumber;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "license_document_url")
    private String licenseDocumentUrl;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "hospital_type")
    private String hospitalType;

    @Column(name = "contact_person")
    private String contactPerson;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(name = "email", nullable = false)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "website")
    private String website;

    @NotBlank(message = "Address is required")
    @Column(name = "address", nullable = false)
    private String address;

    @NotBlank(message = "City is required")
    @Column(name = "city", nullable = false)
    private String city;

    @NotBlank(message = "State is required")
    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean verified = false;

    @Builder.Default
    @Column(name = "verification_status", nullable = false)
    private String verificationStatus = "PENDING";

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_by")
    private String rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Builder.Default
    @Column(name = "emergency_available", nullable = false)
    private Boolean emergencyAvailable = true;

    @Builder.Default
    @Column(name = "operating_hours", nullable = false)
    private String operatingHours = "24/7";

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
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public String getLicenseDocumentUrl() { return licenseDocumentUrl; }
    public void setLicenseDocumentUrl(String licenseDocumentUrl) { this.licenseDocumentUrl = licenseDocumentUrl; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getHospitalType() { return hospitalType; }
    public void setHospitalType(String hospitalType) { this.hospitalType = hospitalType; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Boolean getVerified() { return verified != null ? verified : false; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * Checks whether this hospital is verified/approved by system administrators.
     * Evaluates both boolean flag and verificationStatus string for absolute consistency.
     *
     * @return true if verified or approved, false otherwise
     */
    public boolean isApprovedOrVerified() {
        return Boolean.TRUE.equals(this.verified) ||
               "APPROVED".equalsIgnoreCase(this.verificationStatus) ||
               "VERIFIED".equalsIgnoreCase(this.verificationStatus);
    }

    /**
     * Updates hospital verification fields to APPROVED/VERIFIED state consistently.
     * Also activates the associated user account.
     *
     * @param adminEmail administrator email approving the hospital
     * @param reviewRemarks optional review remarks
     */
    public void markAsApproved(String adminEmail, String reviewRemarks) {
        this.verified = true;
        this.verificationStatus = "APPROVED";
        this.status = "ACTIVE";
        this.verifiedBy = adminEmail;
        this.verifiedAt = LocalDateTime.now();
        this.approvedBy = adminEmail;
        this.approvedAt = LocalDateTime.now();
        this.remarks = (reviewRemarks != null && !reviewRemarks.isBlank()) ? reviewRemarks : "Approved by Administrator";
    }

    /**
     * Updates hospital verification fields to REJECTED state consistently.
     * Also deactivates the associated user account.
     *
     * @param adminEmail administrator email rejecting the hospital
     * @param reason rejection reason
     */
    public void markAsRejected(String adminEmail, String reason) {
        this.verified = false;
        this.verificationStatus = "REJECTED";
        this.status = "INACTIVE";
        this.rejectedBy = adminEmail;
        this.rejectedAt = LocalDateTime.now();
        this.rejectionReason = (reason != null && !reason.isBlank()) ? reason : "Registration rejected by Administrator";
        this.remarks = this.rejectionReason;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
