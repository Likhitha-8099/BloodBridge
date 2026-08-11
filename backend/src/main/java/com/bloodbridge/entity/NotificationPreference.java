package com.bloodbridge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity representing user notification delivery and category preferences.
 */
@Entity
@Table(name = "notification_preferences", indexes = {
        @Index(name = "idx_pref_user_id", columnList = "user_id", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull(message = "User is required")
    private User user;

    @Builder.Default
    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @Builder.Default
    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = true;

    @Builder.Default
    @Column(name = "web_socket_enabled", nullable = false)
    private Boolean webSocketEnabled = true;

    @Builder.Default
    @Column(name = "emergency_alerts_enabled", nullable = false)
    private Boolean emergencyAlertsEnabled = true;

    @Builder.Default
    @Column(name = "reward_notifications_enabled", nullable = false)
    private Boolean rewardNotificationsEnabled = true;

    @Builder.Default
    @Column(name = "reminder_notifications_enabled", nullable = false)
    private Boolean reminderNotificationsEnabled = true;

    @Builder.Default
    @Column(name = "admin_messages_enabled", nullable = false)
    private Boolean adminMessagesEnabled = true;

    @Builder.Default
    @Column(name = "quiet_hours_enabled", nullable = false)
    private Boolean quietHoursEnabled = false;

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @Builder.Default
    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "UTC";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void validatePreferences() {
        // Emergency alerts must always be enabled per requirements
        this.emergencyAlertsEnabled = true;
        if (this.emailEnabled == null) this.emailEnabled = true;
        if (this.pushEnabled == null) this.pushEnabled = true;
        if (this.webSocketEnabled == null) this.webSocketEnabled = true;
        if (this.rewardNotificationsEnabled == null) this.rewardNotificationsEnabled = true;
        if (this.reminderNotificationsEnabled == null) this.reminderNotificationsEnabled = true;
        if (this.adminMessagesEnabled == null) this.adminMessagesEnabled = true;
        if (this.quietHoursEnabled == null) this.quietHoursEnabled = false;
        if (this.timezone == null || this.timezone.trim().isEmpty()) this.timezone = "UTC";
    }
}
