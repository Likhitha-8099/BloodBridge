package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a notification item on the Hospital Dashboard.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Notification Item Payload")
public class NotificationDTO {

    @Schema(description = "Notification ID", example = "301")
    private Long id;

    @Schema(description = "Notification Title", example = "Emergency Request Matched")
    private String title;

    @Schema(description = "Notification Message", example = "3 compatible O+ donors matched for Request #101")
    private String message;

    @Schema(description = "Created Timestamp", example = "2026-08-01T15:20:00")
    private LocalDateTime time;

    @Schema(description = "Unread Indicator Flag", example = "true")
    private Boolean read;

    @Schema(description = "Notification Type", example = "EMERGENCY_BLOOD_REQUEST")
    private String type;
}
