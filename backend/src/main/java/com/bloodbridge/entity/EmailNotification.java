package com.bloodbridge.entity;

import com.bloodbridge.enums.EmailDeliveryStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing an emergency email notification dispatch record for tracking,
 * duplicate prevention, and retry management.
 */
@Entity
@Table(name = "email_notifications", indexes = {
        @Index(name = "idx_email_notif_req_donor", columnList = "emergency_request_id, donor_id", unique = true),
        @Index(name = "idx_email_notif_req_id", columnList = "emergency_request_id"),
        @Index(name = "idx_email_notif_donor_id", columnList = "donor_id"),
        @Index(name = "idx_email_notif_status", columnList = "status"),
        @Index(name = "idx_email_notif_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Emergency request ID is required")
    @Column(name = "emergency_request_id", nullable = false)
    private Long emergencyRequestId;

    @NotNull(message = "Donor ID is required")
    @Column(name = "donor_id", nullable = false)
    private Long donorId;

    @Email
    @Column(name = "email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmailDeliveryStatus status;

    @Builder.Default
    @Column(name = "delivery_attempts", nullable = false)
    private Integer deliveryAttempts = 0;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "smtp_response_time_ms")
    private Long smtpResponseTimeMs;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
