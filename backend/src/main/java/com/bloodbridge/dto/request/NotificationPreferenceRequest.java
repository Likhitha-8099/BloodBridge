package com.bloodbridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Request payload for configuring user notification preferences.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Notification Preferences Update Request")
public class NotificationPreferenceRequest {

    @Schema(description = "Enable email notifications", example = "true")
    private Boolean emailEnabled;

    @Schema(description = "Enable push notifications", example = "true")
    private Boolean pushEnabled;

    @Schema(description = "Enable web socket notifications", example = "true")
    private Boolean webSocketEnabled;

    @Schema(description = "Emergency alerts (Always ON)", example = "true")
    private Boolean emergencyAlertsEnabled;

    @Schema(description = "Enable reward notifications", example = "true")
    private Boolean rewardNotificationsEnabled;

    @Schema(description = "Enable reminder notifications", example = "true")
    private Boolean reminderNotificationsEnabled;

    @Schema(description = "Enable admin messages", example = "true")
    private Boolean adminMessagesEnabled;

    @Schema(description = "Enable quiet hours", example = "true")
    private Boolean quietHoursEnabled;

    @Schema(description = "Quiet hours start time (HH:mm)", example = "22:00")
    private String quietHoursStart;

    @Schema(description = "Quiet hours end time (HH:mm)", example = "07:00")
    private String quietHoursEnd;

    @Schema(description = "User timezone", example = "Asia/Kolkata")
    private String timezone;
}
