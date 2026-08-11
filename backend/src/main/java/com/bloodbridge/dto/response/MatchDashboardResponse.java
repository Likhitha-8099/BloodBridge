package com.bloodbridge.dto.response;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.UrgencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object representing Match Dashboard response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Match Dashboard Response Payload")
public class MatchDashboardResponse {

    @Schema(description = "Blood request ID", example = "10")
    private Long requestId;

    @Schema(description = "Required blood group", example = "O_NEGATIVE")
    private BloodGroup bloodGroupNeeded;

    @Schema(description = "Units required", example = "3")
    private Integer unitsRequired;

    @Schema(description = "Urgency level", example = "CRITICAL")
    private UrgencyLevel urgencyLevel;

    @Schema(description = "Total matches generated", example = "12")
    private int totalMatchesCount;

    @Schema(description = "Ranked list of top matched donors")
    private List<MatchResponse> topMatches;
}
