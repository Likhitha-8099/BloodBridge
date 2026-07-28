package com.bloodbridge.entity;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.enums.UrgencyLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a blood donation request in the system.
 * Connects patients (requesters) and hospitals (verification nodes).
 */
@Entity
@Table(name = "blood_requests")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"patient", "hospital"})
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "Patient profile reference is required")
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    @NotNull(message = "Hospital reference is required")
    private Hospital hospital;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group_needed", nullable = false)
    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroupNeeded;

    @Column(name = "units_required", nullable = false)
    @NotNull(message = "Units required is required")
    @Min(value = 1, message = "Units required must be at least 1")
    private Integer unitsRequired;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level", nullable = false)
    @NotNull(message = "Urgency level is required")
    private UrgencyLevel urgencyLevel;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "request_date", nullable = false)
    @NotNull(message = "Request date is required")
    private LocalDateTime requestDate;

    @Column(name = "required_by_date", nullable = false)
    @NotNull(message = "Required by date is required")
    private LocalDate requiredByDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Request status is required")
    private RequestStatus status;

    @Column(name = "notes", length = 1000)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
