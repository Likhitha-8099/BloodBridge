package com.bloodbridge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity for tracking delivery status, latency, and diagnostics of every FCM Push Notification.
 * Phase 3B.2 — Enterprise Firebase Push Notification Delivery Engine.
 */
@Entity
@Table(
    name = "push_delivery_logs",
    indexes = {
        @Index(name = "idx_push_log_status", columnList = "status"),
        @Index(name = "idx_push_log_user",   columnList = "user_id"),
        @Index(name = "idx_push_log_req",    columnList = "emergency_request_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "deviceToken"})
public class PushDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "emergency_request_id")
    private Long emergencyRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_token_id")
    private DeviceToken deviceToken;

    @NotBlank
    @Column(name = "fcm_token", nullable = false, length = 512)
    private String fcmToken;

    /**
     * Delivery Status: SENT, DELIVERED, FAILED, INVALID_TOKEN
     */
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void onPrePersist() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        if (this.status == null) {
            this.status = "SENT";
        }
    }
}
