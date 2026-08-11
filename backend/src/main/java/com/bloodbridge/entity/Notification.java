package com.bloodbridge.entity;

import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationCategory;
import com.bloodbridge.enums.NotificationPriority;
import com.bloodbridge.enums.NotificationStatus;
import com.bloodbridge.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a notification dispatched or queued for a user in the system.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notif_user_unread", columnList = "recipient_user_id, read_status, deleted"),
        @Index(name = "idx_notif_user_created", columnList = "recipient_user_id, created_at"),
        @Index(name = "idx_notif_category", columnList = "category"),
        @Index(name = "idx_notif_priority", columnList = "priority"),
        @Index(name = "idx_notif_user_cat_prio", columnList = "recipient_user_id, category, priority")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"recipientUser", "hospital", "donor", "patient", "bloodRequest"})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    @NotNull(message = "Recipient user reference is required")
    private User recipientUser;

    @Column(name = "recipient_role")
    private String recipientRole;

    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = "Message is required")
    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Notification type is required")
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    private NotificationCategory category;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Delivery channel is required")
    @Column(name = "delivery_channel", nullable = false, length = 30)
    private DeliveryChannel deliveryChannel;

    @Builder.Default
    @Column(name = "priority", nullable = false)
    private String priority = "NORMAL";

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_enum", length = 20)
    private NotificationPriority priorityEnum;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Notification status is required")
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;

    @Builder.Default
    @JsonProperty("isRead")
    @Column(name = "read_status", nullable = false)
    private Boolean readStatus = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_profile_id")
    private DonorProfile donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_profile_id")
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_request_id")
    private BloodRequest bloodRequest;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "related_entity_type")
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "last_failure_reason", length = 1000)
    private String lastFailureReason;

    @Column(name = "next_retry_time")
    private LocalDateTime nextRetryTime;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("isRead")
    public Boolean getIsRead() {
        return readStatus != null ? readStatus : false;
    }

    public void setIsRead(Boolean isRead) {
        this.readStatus = isRead;
    }

    public String getBody() {
        return message;
    }

    public void setBody(String body) {
        this.message = body;
    }

    @PrePersist
    public void prePersist() {
        if (this.priority == null) {
            this.priority = "NORMAL";
        }
        if (this.priorityEnum == null) {
            try {
                this.priorityEnum = NotificationPriority.valueOf(this.priority.toUpperCase());
            } catch (Exception e) {
                this.priorityEnum = NotificationPriority.NORMAL;
            }
        }
        if (this.category == null) {
            inferCategory();
        }
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        if (this.readStatus == null) {
            this.readStatus = false;
        }
        if (this.deleted == null) {
            this.deleted = false;
        }
        if (this.status == null) {
            this.status = NotificationStatus.PENDING;
        }
        if (this.deliveryChannel == null) {
            this.deliveryChannel = DeliveryChannel.IN_APP;
        }
        if (this.notificationType == null) {
            this.notificationType = NotificationType.SYSTEM_NOTIFICATION;
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.expiryTime == null && (this.category == NotificationCategory.EMERGENCY || this.notificationType == NotificationType.EMERGENCY_BLOOD_REQUEST)) {
            this.expiryTime = this.createdAt.plusHours(24);
        }
        if (this.recipientRole == null && this.recipientUser != null && this.recipientUser.getRole() != null) {
            this.recipientRole = this.recipientUser.getRole().name();
        }
    }

    public void inferCategory() {
        if (this.category != null) return;
        if (this.notificationType == null) {
            this.category = NotificationCategory.SYSTEM;
            return;
        }
        switch (this.notificationType) {
            case EMERGENCY_BLOOD_REQUEST:
                this.category = NotificationCategory.EMERGENCY;
                break;
            case DONATION_ACCEPTED:
            case DONATION_CONFIRMED:
            case HOSPITAL_APPROVAL:
                this.category = NotificationCategory.DONATION_APPROVED;
                break;
            case DONATION_COMPLETED:
                this.category = NotificationCategory.DONATION_COMPLETED;
                break;
            case REQUEST_REJECTED:
            case DONOR_DECLINED:
            case DONOR_REJECTED:
                this.category = NotificationCategory.REQUEST_CANCELLED;
                break;
            case SYSTEM_ANNOUNCEMENT:
                this.category = NotificationCategory.ADMIN;
                break;
            case DONATION_REMINDER:
                this.category = NotificationCategory.REMINDER;
                break;
            default:
                this.category = NotificationCategory.SYSTEM;
                break;
        }
    }
}
