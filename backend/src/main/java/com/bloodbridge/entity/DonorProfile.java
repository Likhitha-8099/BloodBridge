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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a donor profile in the Blood Bridge system.
 * Keeps track of blood group, age, donation history, and eligibility parameters.
 */
@Entity
@Table(name = "donor_profiles")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @Column(name = "age", nullable = false)
    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 60, message = "Age cannot exceed 60")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "City is required")
    @Column(name = "city", nullable = false)
    private String city;

    @NotBlank(message = "State is required")
    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;

    @Builder.Default
    @Column(name = "available_for_donation", nullable = false)
    private Boolean availableForDonation = true;

    @Column(name = "medical_conditions", length = 1000)
    private String medicalConditions;

    @Column(name = "weight", nullable = false)
    @NotNull(message = "Weight is required")
    @Min(value = 50, message = "Weight must be at least 50 kg")
    private Double weight;

    @Builder.Default
    @Column(name = "total_donations", nullable = false)
    private Integer totalDonations = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
