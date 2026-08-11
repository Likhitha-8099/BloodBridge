package com.bloodbridge.service.impl;

import com.bloodbridge.entity.AuditLog;
import com.bloodbridge.repository.AuditLogRepository;
import com.bloodbridge.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of AuditLogService for recording immutable security and execution audit trails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public AuditLog logAction(String userEmail, String userRole, Long emergencyRequestId, Long hospitalId, Long donorId, String action, String details, String ipAddress, Long executionTimeMs) {
        log.info("[AUDIT-LOG] User: {} ({}) | Action: {} | Emergency #: {} | Time: {} ms",
                userEmail, userRole, action, emergencyRequestId, executionTimeMs);

        AuditLog logEntry = AuditLog.builder()
                .userEmail(userEmail)
                .userRole(userRole)
                .emergencyRequestId(emergencyRequestId)
                .hospitalId(hospitalId)
                .donorId(donorId)
                .action(action)
                .details(details)
                .ipAddress(ipAddress)
                .executionTimeMs(executionTimeMs)
                .createdAt(LocalDateTime.now())
                .build();

        return auditLogRepository.save(logEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsForRequest(Long emergencyRequestId) {
        return auditLogRepository.findByEmergencyRequestIdOrderByCreatedAtDesc(emergencyRequestId);
    }
}
