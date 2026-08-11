package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a recent blood request item on the Hospital Dashboard.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Recent Blood Request Summary Payload")
public class RecentRequestDTO {

    @Schema(description = "Request ID", example = "101")
    private Long id;

    @Schema(description = "Patient Name", example = "Sarah Connor")
    private String patientName;

    @Schema(description = "Blood Group Needed", example = "O_POSITIVE")
    private String bloodGroup;

    @Schema(description = "Units Required", example = "2")
    private Integer units;

    @Schema(description = "Urgency / Priority Level", example = "EMERGENCY")
    private String priority;

    @Schema(description = "Request Status", example = "PENDING")
    private String status;

    @Schema(description = "Created Date & Time", example = "2026-08-01T10:30:00")
    private LocalDateTime createdDate;

    @Schema(description = "Estimated time remaining string for emergency requests", example = "2 hrs left")
    private String timeRemaining;
}
