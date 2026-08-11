package com.bloodbridge.entity;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a patient profile in the Blood Bridge system.
 */
@Entity
@Table(name = "patient_profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "hospital"})
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull(message = "User reference is required")
    private User user;

    @Column(name = "patient_code")
    private String patientCode;

    @Column(name = "age", nullable = false)
    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 120, message = "Age cannot exceed 120")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    @NotNull(message = "Gender is required")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @Column(name = "rh_factor")
    private String rhFactor;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "medical_condition", length = 1000)
    private String medicalCondition;

    @Column(name = "diagnosis", length = 1000)
    private String diagnosis;

    @Column(name = "doctor_name")
    private String doctorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @NotBlank(message = "Emergency contact name is required")
    @Column(name = "emergency_contact_name", nullable = false)
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact number is required")
    @Column(name = "emergency_contact_number", nullable = false)
    private String emergencyContactNumber;

    @Column(name = "relationship")
    private String relationship;

    @Column(name = "address")
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

    @Column(name = "preferred_hospital")
    private String preferredHospital;

    @Column(name = "medical_history", length = 2000)
    private String medicalHistory;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
