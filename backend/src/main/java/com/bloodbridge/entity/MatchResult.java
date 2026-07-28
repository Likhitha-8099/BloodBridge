package com.bloodbridge.entity;

import com.bloodbridge.enums.MatchStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a matching result between a blood request and an eligible donor.
 */
@Entity
@Table(name = "match_results")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"bloodRequest", "donor"})
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_request_id", nullable = false)
    @NotNull(message = "Blood request reference is required")
    private BloodRequest bloodRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_profile_id", nullable = false)
    @NotNull(message = "Donor profile reference is required")
    private DonorProfile donor;

    @NotNull(message = "Compatibility score is required")
    @Column(name = "compatibility_score", nullable = false)
    private Integer compatibilityScore;

    @NotNull(message = "Matched timestamp is required")
    @Column(name = "matched_at", nullable = false)
    private LocalDateTime matchedAt;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Match status is required")
    @Column(name = "status", nullable = false)
    private MatchStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
