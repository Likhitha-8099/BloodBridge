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
    @JoinColumn(name = "patient_id", nullable = true)
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
    @Column(name = "status", nullable = false, length = 50)
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PatientProfile getPatient() { return patient; }
    public void setPatient(PatientProfile patient) { this.patient = patient; }
    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = hospital; }
    public BloodGroup getBloodGroupNeeded() { return bloodGroupNeeded; }
    public void setBloodGroupNeeded(BloodGroup bloodGroupNeeded) { this.bloodGroupNeeded = bloodGroupNeeded; }
    public Integer getUnitsRequired() { return unitsRequired; }
    public void setUnitsRequired(Integer unitsRequired) { this.unitsRequired = unitsRequired; }
    public UrgencyLevel getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(UrgencyLevel urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public LocalDate getRequiredByDate() { return requiredByDate; }
    public void setRequiredByDate(LocalDate requiredByDate) { this.requiredByDate = requiredByDate; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isTerminalState() {
        return this.status == RequestStatus.COMPLETED ||
               this.status == RequestStatus.CANCELLED ||
               this.status == RequestStatus.EXPIRED;
    }

    public boolean canAcceptDonor() {
        if (isTerminalState() || this.status == RequestStatus.REJECTED) {
            return false;
        }
        if (this.requiredByDate != null && this.requiredByDate.isBefore(LocalDate.now())) {
            return false;
        }
        return true;
    }

    public boolean canConfirmDonor() {
        if (isTerminalState() || this.status == RequestStatus.REJECTED) {
            return false;
        }
        return this.status == RequestStatus.ACTIVE ||
               this.status == RequestStatus.DONOR_ACCEPTED ||
               this.status == RequestStatus.PENDING ||
               this.status == RequestStatus.VERIFIED ||
               this.status == RequestStatus.MATCHED ||
               this.status == RequestStatus.DONOR_NOTIFIED ||
               this.status == RequestStatus.CREATED ||
               this.status == RequestStatus.MATCHING;
    }

    public boolean canTransitionTo(RequestStatus targetStatus) {
        if (this.status == targetStatus) return true;

        if (isTerminalState()) {
            // Cannot transition from terminal states back to active states
            return false;
        }

        switch (targetStatus) {
            case CREATED:
                return false; // Cannot transition back to CREATED
            case ACTIVE:
            case PENDING:
            case VERIFIED:
            case MATCHING:
            case MATCHED:
            case DONOR_NOTIFIED:
                return this.status == RequestStatus.CREATED || this.status == RequestStatus.PENDING || this.status == RequestStatus.VERIFIED || this.status == RequestStatus.ACTIVE || this.status == RequestStatus.MATCHING;
            case DONOR_ACCEPTED:
                return this.status == RequestStatus.CREATED || this.status == RequestStatus.PENDING || this.status == RequestStatus.ACTIVE || this.status == RequestStatus.VERIFIED || this.status == RequestStatus.MATCHING || this.status == RequestStatus.MATCHED || this.status == RequestStatus.DONOR_NOTIFIED || this.status == RequestStatus.DONOR_ACCEPTED;
            case FULFILLMENT_IN_PROGRESS:
            case IN_PROGRESS:
                return this.status == RequestStatus.DONOR_ACCEPTED || this.status == RequestStatus.ACTIVE || this.status == RequestStatus.VERIFIED || this.status == RequestStatus.MATCHED || this.status == RequestStatus.DONOR_NOTIFIED || this.status == RequestStatus.PENDING;
            case FULFILLED:
                return this.status == RequestStatus.FULFILLMENT_IN_PROGRESS || this.status == RequestStatus.IN_PROGRESS || this.status == RequestStatus.DONOR_ACCEPTED;
            case COMPLETED:
                return this.status == RequestStatus.FULFILLED || this.status == RequestStatus.FULFILLMENT_IN_PROGRESS || this.status == RequestStatus.IN_PROGRESS || this.status == RequestStatus.DONOR_ACCEPTED || this.status == RequestStatus.ACTIVE || this.status == RequestStatus.VERIFIED;
            case CANCELLED:
            case EXPIRED:
            case REJECTED:
                return true;
            default:
                return false;
        }
    }
}
