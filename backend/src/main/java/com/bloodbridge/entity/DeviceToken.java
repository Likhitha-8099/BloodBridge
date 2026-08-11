package com.bloodbridge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a Firebase Cloud Messaging device token registered
 * by an authenticated user. One user may have many device tokens
 * (one per browser/device). One FCM token maps to exactly one row.
 *
 * <p>Phase 3B.1 — Device Registration module.</p>
 */
@Entity
@Table(
    name = "device_tokens",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_device_token", columnNames = "fcm_token")
    },
    indexes = {
        @Index(name = "idx_device_user",      columnList = "user_id"),
        @Index(name = "idx_device_active",    columnList = "is_active"),
        @Index(name = "idx_device_last_seen", columnList = "last_seen")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The authenticated user who owns this device token. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The Firebase Cloud Messaging registration token for this browser/device.
     * Unique across the entire table — one row per token.
     */
    @NotBlank(message = "FCM token is required")
    @Column(name = "fcm_token", nullable = false, unique = true, length = 512)
    private String fcmToken;

    /** Platform identifier, e.g. WEB, ANDROID, IOS. */
    @Builder.Default
    @Column(name = "platform", nullable = false, length = 20)
    private String platform = "WEB";

    /** Browser name parsed from User-Agent, e.g. Chrome, Firefox. */
    @Column(name = "browser", length = 50)
    private String browser;

    /** Human-readable device name, e.g. "John's MacBook". */
    @Column(name = "device_name", length = 100)
    private String deviceName;

    /** Client-generated stable device fingerprint (optional). */
    @Column(name = "device_id", length = 200)
    private String deviceId;

    /** Whether this token is currently active. */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** UTC timestamp of the last successful token validation / heartbeat. */
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void onPrePersist() {
        if (this.lastSeen == null) {
            this.lastSeen = LocalDateTime.now();
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.platform == null) {
            this.platform = "WEB";
        }
    }
}
