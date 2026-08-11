package com.bloodbridge.dto.response;

import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.RequestStatus;
import com.bloodbridge.enums.UrgencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing the detailed response of a blood request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Blood Request Response Payload")
public class BloodRequestResponse {

    @Schema(description = "Blood request ID", example = "10")
    private Long id;

    @Schema(description = "Patient ID if associated", example = "5")
    private Long patientId;

    @Schema(description = "Patient full name", example = "Jane Patient")
    private String patientName;

    @Schema(description = "Hospital ID", example = "1")
    private Long hospitalId;

    @Schema(description = "Hospital name", example = "Boston General Hospital")
    private String hospitalName;

    @Schema(description = "Blood group needed", example = "O_NEGATIVE")
    private BloodGroup bloodGroupNeeded;

    @Schema(description = "Units required", example = "3")
    private Integer unitsRequired;

    @Schema(description = "Urgency level", example = "CRITICAL")
    private UrgencyLevel urgencyLevel;

    @Schema(description = "Reason or diagnosis", example = "Emergency Trauma Surgery")
    private String reason;

    @Schema(description = "Request date", example = "2026-08-01T11:00:00")
    private LocalDateTime requestDate;

    @Schema(description = "Required by date", example = "2026-08-02")
    private LocalDate requiredByDate;

    @Schema(description = "Request status", example = "CREATED")
    private RequestStatus status;

    @Schema(description = "Notes or instructions", example = "Report to Room 402 Blood Bank")
    private String notes;

    @Schema(description = "Created timestamp", example = "2026-08-01T11:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp", example = "2026-08-01T11:00:00")
    private LocalDateTime updatedAt;
}
