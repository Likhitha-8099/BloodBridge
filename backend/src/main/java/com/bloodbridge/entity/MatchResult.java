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
@Table(name = "match_results", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"blood_request_id", "donor_profile_id"})
})
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

    @Column(name = "match_score", nullable = false)
    private Double matchScore;

    @Column(name = "blood_compatibility_score")
    private Double bloodCompatibilityScore;

    @Column(name = "compatibility_score")
    private Double compatibilityScore;

    @Column(name = "distance_score")
    private Double distanceScore;

    @Column(name = "availability_score")
    private Double availabilityScore;

    @Column(name = "donor_score")
    private Double donorScore;

    @Column(name = "eligibility_status")
    private String eligibilityStatus;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "estimated_travel_time")
    private Integer estimatedTravelTime;

    @Column(name = "rank_position")
    private Integer rank;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Match status is required")
    @Column(name = "status", nullable = false)
    private MatchStatus status;

    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BloodRequest getBloodRequest() { return bloodRequest; }
    public void setBloodRequest(BloodRequest bloodRequest) { this.bloodRequest = bloodRequest; }
    public DonorProfile getDonor() { return donor; }
    public void setDonor(DonorProfile donor) { this.donor = donor; }
    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
}
