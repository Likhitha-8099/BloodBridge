package com.bloodbridge.dto.response;

import com.bloodbridge.enums.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object representing emergency request lifecycle progress timeline.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Blood Request Timeline Response Payload")
public class RequestTimelineResponse {

    @Schema(description = "Blood request ID", example = "10")
    private Long requestId;

    @Schema(description = "Current request status", example = "DONOR_ACCEPTED")
    private RequestStatus currentStatus;

    @Schema(description = "Chronological timeline steps")
    private List<TimelineStep> timeline;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Timeline Event Step")
    public static class TimelineStep {
        @Schema(description = "Step title", example = "Request Created")
        private String stepName;

        @Schema(description = "Timestamp of step completion", example = "2026-08-01T10:00:00")
        private LocalDateTime timestamp;

        @Schema(description = "Step status (COMPLETED, IN_PROGRESS, PENDING)", example = "COMPLETED")
        private String status;

        @Schema(description = "Detailed notes or event description", example = "Emergency blood request registered by patient.")
        private String details;
    }
}
