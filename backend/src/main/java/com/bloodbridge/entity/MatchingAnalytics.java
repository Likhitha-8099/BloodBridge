package com.bloodbridge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing telemetry and historical metrics recorded during Smart Donor Matching Pipeline runs (Stage 8).
 */
@Entity
@Table(name = "matching_analytics", indexes = {
        @Index(name = "idx_match_analytics_request", columnList = "blood_request_id"),
        @Index(name = "idx_match_analytics_hospital", columnList = "hospital_id"),
        @Index(name = "idx_match_analytics_created", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blood_request_id", nullable = false)
    private Long bloodRequestId;

    @Column(name = "hospital_id")
    private Long hospitalId;

    @Column(name = "blood_group", nullable = false)
    private String bloodGroup;

    @Column(name = "compatible_donors_found")
    private long compatibleDonorsFound;

    @Column(name = "eligible_donors_count")
    private long eligibleDonorsCount;

    @Column(name = "filtered_donors_count")
    private long filteredDonorsCount;

    @Column(name = "group_a_donors_count")
    private long groupADonorsCount;

    @Column(name = "group_b_donors_count")
    private long groupBDonorsCount;

    @Column(name = "group_c_donors_count")
    private long groupCDonorsCount;

    @Column(name = "group_d_donors_count")
    private long groupDDonorsCount;

    @Column(name = "notification_batches_sent")
    private int notificationBatchesSent;

    @Column(name = "response_rate")
    private double responseRate;

    @Column(name = "average_response_time_seconds")
    private double averageResponseTimeSeconds;

    @Column(name = "matching_duration_ms")
    private long matchingDurationMs;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
