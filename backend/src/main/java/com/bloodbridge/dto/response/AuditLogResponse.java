package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing Audit Log entries.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Audit Log Response Payload")
public class AuditLogResponse {

    @Schema(description = "Audit Log ID", example = "101")
    private Long id;

    @Schema(description = "User email", example = "admin@bloodbridge.com")
    private String userEmail;

    @Schema(description = "Audit action name", example = "HOSPITAL_APPROVED")
    private String action;

    @Schema(description = "Module name", example = "HOSPITAL_MODULE")
    private String module;

    @Schema(description = "Detailed log context", example = "Approved Hospital ID 1 registration.")
    private String description;

    @Schema(description = "IP address", example = "127.0.0.1")
    private String ipAddress;

    @Schema(description = "Browser user agent", example = "Mozilla/5.0")
    private String browser;

    @Schema(description = "Execution status", example = "SUCCESS")
    private String status;

    @Schema(description = "Log severity level", example = "INFO")
    private String severity;

    @Schema(description = "Log timestamp", example = "2026-08-01T11:30:00")
    private LocalDateTime timestamp;
}
