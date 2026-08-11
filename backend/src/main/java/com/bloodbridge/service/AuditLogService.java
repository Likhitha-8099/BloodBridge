package com.bloodbridge.service;

import com.bloodbridge.entity.AuditLog;

import java.util.List;

/**
 * Service interface for recording security and operational audit logs.
 */
public interface AuditLogService {

    AuditLog logAction(String userEmail, String userRole, Long emergencyRequestId, Long hospitalId, Long donorId, String action, String details, String ipAddress, Long executionTimeMs);

    List<AuditLog> getLogsForRequest(Long emergencyRequestId);
}
