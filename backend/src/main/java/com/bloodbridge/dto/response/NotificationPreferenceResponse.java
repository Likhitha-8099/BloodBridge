package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for user notification preferences.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Notification Preferences Response Payload")
public class NotificationPreferenceResponse {

    @Schema(description = "Preference ID", example = "1")
    private Long id;

    @Schema(description = "User ID", example = "5")
    private Long userId;

    @Schema(description = "Email notification enabled", example = "true")
    private Boolean emailEnabled;

    @Schema(description = "Push notification enabled", example = "true")
    private Boolean pushEnabled;

    @Schema(description = "WebSocket notification enabled", example = "true")
    private Boolean webSocketEnabled;

    @Schema(description = "Emergency alerts (Always ON)", example = "true")
    private Boolean emergencyAlertsEnabled;

    @Schema(description = "Reward notifications enabled", example = "true")
    private Boolean rewardNotificationsEnabled;

    @Schema(description = "Reminder notifications enabled", example = "true")
    private Boolean reminderNotificationsEnabled;

    @Schema(description = "Admin messages enabled", example = "true")
    private Boolean adminMessagesEnabled;

    @Schema(description = "Quiet hours enabled", example = "false")
    private Boolean quietHoursEnabled;

    @Schema(description = "Quiet hours start time", example = "22:00")
    private LocalTime quietHoursStart;

    @Schema(description = "Quiet hours end time", example = "07:00")
    private LocalTime quietHoursEnd;

    @Schema(description = "Timezone", example = "UTC")
    private String timezone;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    private LocalDateTime updatedAt;
}
