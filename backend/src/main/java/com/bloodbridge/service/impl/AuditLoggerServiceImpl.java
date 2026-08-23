package com.bloodbridge.service.impl;

import com.bloodbridge.entity.AuditLog;
import com.bloodbridge.repository.AuditLogRepository;
import com.bloodbridge.service.AuditLoggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service implementation for Audit Logging with DB persistence.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLoggerServiceImpl implements AuditLoggerService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logEvent(String action, String email, String details) {
        log.info("[AUDIT EVENT] Timestamp: {} | Action: {} | User: {} | Context: {}",
                LocalDateTime.now(), action, email, details);

        try {
            AuditLog auditLog = AuditLog.builder()
                    .userEmail(email)
                    .action(action)
                    .module(resolveModuleFromAction(action))
                    .description(details)
                    .status("SUCCESS")
                    .severity("INFO")
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("[AUDIT LOG SAVE WARNING] Could not persist audit log record: {}", e.getMessage());
        }
    }

    private String resolveModuleFromAction(String action) {
        if (action == null) return "SYSTEM";
        if (action.contains("HOSPITAL")) return "HOSPITAL_MODULE";
        if (action.contains("DONOR")) return "DONOR_MODULE";
        if (action.contains("PATIENT")) return "PATIENT_MODULE";
        if (action.contains("REQUEST")) return "BLOOD_REQUEST_MODULE";
        if (action.contains("MATCH")) return "MATCHING_MODULE";
        if (action.contains("NOTIFICATION") || action.contains("ANNOUNCEMENT")) return "NOTIFICATION_MODULE";
        if (action.contains("USER")) return "USER_MODULE";
        return "SYSTEM";
    }
}
