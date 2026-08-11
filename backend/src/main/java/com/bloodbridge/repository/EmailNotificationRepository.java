package com.bloodbridge.repository;

import com.bloodbridge.entity.EmailNotification;
import com.bloodbridge.enums.EmailDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing EmailNotification entities.
 */
@Repository
public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Long> {

    boolean existsByEmergencyRequestIdAndDonorId(Long emergencyRequestId, Long donorId);

    Optional<EmailNotification> findByEmergencyRequestIdAndDonorId(Long emergencyRequestId, Long donorId);

    List<EmailNotification> findByEmergencyRequestId(Long emergencyRequestId);

    long countByEmergencyRequestIdAndStatus(Long emergencyRequestId, EmailDeliveryStatus status);

    long countByStatus(EmailDeliveryStatus status);

    List<EmailNotification> findByStatusInAndDeliveryAttemptsLessThan(List<EmailDeliveryStatus> statuses, int maxAttempts);

    @Query("SELECT AVG(e.smtpResponseTimeMs) FROM EmailNotification e WHERE e.emergencyRequestId = :requestId AND e.status = 'SENT'")
    Double findAverageSmtpTimeMsByEmergencyRequestId(@Param("requestId") Long requestId);
}
