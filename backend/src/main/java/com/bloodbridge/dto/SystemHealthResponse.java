package com.bloodbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing system health diagnostics.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemHealthResponse {
    private String databaseConnectivity;
    private Long totalRecords;
    private Long activeUsers;
    private String notificationQueueStatus;
    private String apiHealth;
}
