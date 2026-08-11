package com.bloodbridge.entity;

import com.bloodbridge.enums.MatchedEmergencyDonorStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Dedicated Entity representing a mapping between an Emergency Blood Request
 * and an eligible matched donor identified by the Smart Donor Matching Engine.
 */
@Entity
@Table(name = "matched_emergency_donors", uniqueConstraints = {
        @UniqueConstraint(name = "uk_matched_emerg_req_donor", columnNames = {"blood_request_id", "donor_id"})
}, indexes = {
        @Index(name = "idx_med_donor_status", columnList = "donor_id, status"),
        @Index(name = "idx_med_request_id", columnList = "blood_request_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"bloodRequest", "donor", "hospital"})
public class MatchedEmergencyDonor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_request_id", nullable = false)
    @NotNull(message = "Blood request reference is required")
    private BloodRequest bloodRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    @NotNull(message = "Donor reference is required")
    private DonorProfile donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    @NotNull(message = "Hospital reference is required")
    private Hospital hospital;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "matching_group", length = 50)
    private String matchingGroup;

    @Column(name = "notification_sent", nullable = false)
    @Builder.Default
    private Boolean notificationSent = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @NotNull(message = "Status is required")
    @Builder.Default
    private MatchedEmergencyDonorStatus status = MatchedEmergencyDonorStatus.PENDING;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "confirmed")
    @Builder.Default
    private Boolean confirmed = false;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "fulfillment_status", length = 50)
    private String fulfillmentStatus;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "confirmed_by_user_id")
    private Long confirmedByUserId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
