package com.bloodbridge.entity;

import com.bloodbridge.enums.EmergencyResponseStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a donor's real-time interactive response to an emergency blood request.
 */
@Entity
@Table(name = "emergency_responses", indexes = {
        @Index(name = "idx_emerg_resp_req_donor", columnList = "emergency_request_id, donor_id", unique = true),
        @Index(name = "idx_emerg_resp_req_status", columnList = "emergency_request_id, status"),
        @Index(name = "idx_emerg_resp_donor_id", columnList = "donor_id"),
        @Index(name = "idx_emerg_resp_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_request_id", nullable = false)
    @NotNull(message = "Blood request reference is required")
    private BloodRequest bloodRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    @NotNull(message = "Donor reference is required")
    private DonorProfile donor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Response status is required")
    private EmergencyResponseStatus status;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "started_travel_at")
    private LocalDateTime startedTravelAt;

    @Column(name = "reached_hospital_at")
    private LocalDateTime reachedHospitalAt;

    @Column(name = "completed_donation_at")
    private LocalDateTime completedDonationAt;

    @Column(name = "reward_generated_at")
    private LocalDateTime rewardGeneratedAt;

    @Column(name = "response_time_seconds")
    private Long responseTimeSeconds;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "eta_minutes")
    private Integer etaMinutes;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
