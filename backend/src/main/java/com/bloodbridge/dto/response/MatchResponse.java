package com.bloodbridge.dto.response;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.MatchStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing detailed Match Result payload.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Match Result Response Payload")
public class MatchResponse {

    @Schema(description = "Match Result ID", example = "1")
    private Long id;

    @Schema(description = "Blood request ID", example = "10")
    private Long requestId;

    @Schema(description = "Donor profile ID", example = "5")
    private Long donorId;

    @Schema(description = "Donor full name", example = "John Smith")
    private String donorName;

    @Schema(description = "Donor phone number", example = "+16175550199")
    private String donorPhoneNumber;

    @Schema(description = "Donor blood group", example = "O_NEGATIVE")
    private BloodGroup bloodGroup;

    @Schema(description = "Calculated overall match score (0-100)", example = "94.5")
    private Double matchScore;

    @Schema(description = "Blood compatibility score", example = "100.0")
    private Double bloodCompatibilityScore;

    @Schema(description = "Distance proximity score", example = "85.0")
    private Double distanceScore;

    @Schema(description = "Availability score", example = "100.0")
    private Double availabilityScore;

    @Schema(description = "Donor engagement score", example = "140.0")
    private Double donorScore;

    @Schema(description = "Eligibility status", example = "ELIGIBLE")
    private String eligibilityStatus;

    @Schema(description = "Distance in KM", example = "4.2")
    private Double distanceKm;

    @Schema(description = "Estimated travel time in minutes", example = "9")
    private Integer estimatedTravelTimeMinutes;

    @Schema(description = "Rank position (1 to N)", example = "1")
    private Integer rank;

    @Schema(description = "Match status (MATCHED, NOTIFIED, ACCEPTED, etc.)", example = "MATCHED")
    private MatchStatus status;

    @Schema(description = "Matched timestamp", example = "2026-08-01T11:00:00")
    private LocalDateTime matchedAt;
}
