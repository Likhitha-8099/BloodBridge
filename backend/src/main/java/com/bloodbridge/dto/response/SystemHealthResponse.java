package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing System Health & Infrastructure Operations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "System Health Response Payload")
public class SystemHealthResponse {

    @Schema(description = "Application status", example = "UP")
    private String status;

    @Schema(description = "Database connectivity status", example = "UP")
    private String dbStatus;

    @Schema(description = "Application uptime in seconds", example = "86400")
    private long uptimeSeconds;

    @Schema(description = "Total allocated memory in MB", example = "512")
    private long totalMemoryMb;

    @Schema(description = "Free memory in MB", example = "256")
    private long freeMemoryMb;

    @Schema(description = "Maximum memory limit in MB", example = "2048")
    private long maxMemoryMb;

    @Schema(description = "Active logged-in users count", example = "18")
    private long activeUsers;

    @Schema(description = "Server current timestamp", example = "2026-08-01T12:00:00")
    private LocalDateTime serverTime;

    @Schema(description = "System version", example = "0.0.1-SNAPSHOT")
    private String version;

    @Schema(description = "Deployment environment", example = "PRODUCTION")
    private String environment;

    @Schema(description = "Database link status", example = "UP")
    private String databaseConnectivity;

    @Schema(description = "API health status", example = "UP")
    private String apiHealth;

    @Schema(description = "Notification alert queue status", example = "ACTIVE")
    private String notificationQueueStatus;

    @Schema(description = "Total system records count", example = "1250")
    private Long totalRecords;
}
