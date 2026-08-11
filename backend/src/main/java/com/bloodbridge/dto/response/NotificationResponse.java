package com.bloodbridge.dto.response;

import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationCategory;
import com.bloodbridge.enums.NotificationPriority;
import com.bloodbridge.enums.NotificationStatus;
import com.bloodbridge.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a detailed Notification response item for Phase 3C.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Notification Response Payload")
public class NotificationResponse {

    @Schema(description = "Notification ID", example = "1")
    private Long id;

    @Schema(description = "Recipient user ID", example = "5")
    private Long userId;

    @Schema(description = "Recipient user ID alias", example = "5")
    private Long recipientUserId;

    @Schema(description = "Emergency Request ID", example = "25")
    private Long emergencyRequestId;

    @Schema(description = "Recipient user role", example = "DONOR")
    private String recipientRole;

    @Schema(description = "Title", example = "Emergency Blood Request Match")
    private String title;

    @Schema(description = "Message content", example = "An urgent blood request matches your profile.")
    private String message;

    @Schema(description = "Body alias for message", example = "An urgent blood request matches your profile.")
    private String body;

    @Schema(description = "Notification type", example = "DONOR_MATCH_FOUND")
    private NotificationType notificationType;

    @Schema(description = "Notification category", example = "EMERGENCY")
    private NotificationCategory category;

    @Schema(description = "Delivery channel", example = "IN_APP")
    private DeliveryChannel deliveryChannel;

    @Schema(description = "Channel alias", example = "IN_APP")
    private DeliveryChannel channel;

    @Schema(description = "Priority level string", example = "CRITICAL")
    private String priority;

    @Schema(description = "Priority enum", example = "CRITICAL")
    private NotificationPriority priorityEnum;

    @Schema(description = "Delivery status", example = "SENT")
    private NotificationStatus status;

    @Schema(description = "Donor ID", example = "3")
    private Long donorId;

    @Schema(description = "Donor Name", example = "John Smith")
    private String donorName;

    @Schema(description = "Donor Blood Group", example = "B_POSITIVE")
    private String bloodGroup;

    @Schema(description = "Blood Request ID", example = "25")
    private Long requestId;

    @Schema(description = "Hospital ID", example = "1")
    private Long hospitalId;

    @Schema(description = "Hospital Name", example = "City Hospital")
    private String hospitalName;

    @Schema(description = "Patient ID", example = "10")
    private Long patientId;

    @Schema(description = "Patient Name", example = "Jane Doe")
    private String patientName;

    @Schema(description = "Action URL for navigation", example = "/donor/requests/25")
    private String actionUrl;

    @Schema(description = "Read status", example = "false")
    private Boolean readStatus;

    @JsonProperty("isRead")
    @Schema(description = "Is read boolean flag", example = "false")
    private Boolean isRead;

    @Schema(description = "Related entity type", example = "BLOOD_REQUEST")
    private String relatedEntityType;

    @Schema(description = "Related entity ID", example = "10")
    private Long relatedEntityId;

    @Schema(description = "Sent timestamp", example = "2026-08-01T11:00:00")
    private LocalDateTime sentAt;

    @Schema(description = "Read timestamp", example = "2026-08-01T11:05:00")
    private LocalDateTime readAt;

    @Schema(description = "Read time alias", example = "2026-08-01T11:05:00")
    private LocalDateTime readTime;

    @Schema(description = "Expiry timestamp", example = "2026-08-02T11:00:00")
    private LocalDateTime expiryTime;

    @Schema(description = "Metadata JSON payload", example = "{\"key\":\"value\"}")
    private String metadataJson;

    @Schema(description = "Created timestamp", example = "2026-08-01T11:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Created time alias", example = "2026-08-01T11:00:00")
    private LocalDateTime createdTime;
}
