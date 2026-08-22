package com.bloodbridge.repository;

import com.bloodbridge.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for AuditLog entity.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEmergencyRequestIdOrderByCreatedAtDesc(Long emergencyRequestId);

    List<AuditLog> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    default Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable) {
        return findAllByOrderByCreatedAtDesc(pageable);
    }

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query(value = "UPDATE audit_logs SET user_id = NULL WHERE user_id = :userId", nativeQuery = true)
    void unlinkUser(@org.springframework.data.repository.query.Param("userId") Long userId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query(value = "UPDATE audit_logs SET donor_id = NULL WHERE donor_id = :donorId", nativeQuery = true)
    void unlinkDonor(@org.springframework.data.repository.query.Param("donorId") Long donorId);
}
