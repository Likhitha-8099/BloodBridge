package com.bloodbridge.repository;

import com.bloodbridge.entity.PushDeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for PushDeliveryLog analytics and metrics.
 * Phase 3B.2 — Enterprise Firebase Push Notification Delivery Engine.
 */
@Repository
public interface PushDeliveryLogRepository extends JpaRepository<PushDeliveryLog, Long> {

    long countByStatus(String status);

    long countByStatusIn(List<String> statuses);

    @Query("SELECT COALESCE(AVG(p.latencyMs), 0.0) FROM PushDeliveryLog p WHERE p.status = 'SENT' OR p.status = 'DELIVERED'")
    double findAverageLatencyMs();

    @Query("SELECT COALESCE(SUM(p.retryCount), 0) FROM PushDeliveryLog p")
    long findTotalRetryCount();

    @Query("SELECT COUNT(DISTINCT p.fcmToken) FROM PushDeliveryLog p WHERE p.status = 'INVALID_TOKEN'")
    long countDistinctInvalidTokens();

    @Query("SELECT p.failureReason, COUNT(p) FROM PushDeliveryLog p WHERE p.failureReason IS NOT NULL GROUP BY p.failureReason ORDER BY COUNT(p) DESC")
    List<Object[]> findTopFailureReasons();

    List<PushDeliveryLog> findByEmergencyRequestId(Long emergencyRequestId);

    long countByCreatedAtAfter(LocalDateTime timestamp);
}
