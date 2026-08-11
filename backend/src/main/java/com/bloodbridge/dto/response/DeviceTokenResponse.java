package com.bloodbridge.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for device token registration and listing operations.
 * Never exposes the raw FCM token value — only metadata and status.
 * Phase 3B.1 — Device Registration.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Registered device token metadata response")
public class DeviceTokenResponse {

    @Schema(description = "Internal device token record ID")
    private Long id;

    @Schema(description = "User ID who owns this token")
    private Long userId;

    @Schema(description = "Platform: WEB, ANDROID, IOS", example = "WEB")
    private String platform;

    @Schema(description = "Browser name parsed from User-Agent", example = "Chrome")
    private String browser;

    @Schema(description = "Human-friendly device name", example = "John's Laptop")
    private String deviceName;

    @Schema(description = "Stable device fingerprint")
    private String deviceId;

    @Schema(description = "Whether this token is currently active")
    private Boolean isActive;

    @Schema(description = "Timestamp of last successful heartbeat/registration")
    private LocalDateTime lastSeen;

    @Schema(description = "When this device token was first registered")
    private LocalDateTime registeredAt;

    // ── Convenience fields for registration responses ──────────────────────

    @Schema(description = "Human-readable result message")
    private String message;

    @Schema(description = "True if a new row was created; false if an existing token was updated")
    private Boolean registered;
}
