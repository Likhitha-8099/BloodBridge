package com.bloodbridge.dto.request;

import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for sending/dispatching a notification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Send Notification Request Payload")
public class SendNotificationRequest {

    @NotNull(message = "Recipient user ID is required")
    @Schema(description = "Target user ID", example = "5")
    private Long recipientUserId;

    @NotBlank(message = "Title is required")
    @Schema(description = "Notification title", example = "Emergency Blood Request Match")
    private String title;

    @NotBlank(message = "Message is required")
    @Schema(description = "Notification body text", example = "An urgent blood request for O- blood matches your profile in Boston General Hospital.")
    private String message;

    @NotNull(message = "Notification type is required")
    @Schema(description = "Notification type enum", example = "DONOR_MATCH_FOUND")
    private NotificationType type;

    @NotNull(message = "Delivery channel is required")
    @Schema(description = "Delivery channel enum (EMAIL, PUSH, IN_APP, SMS)", example = "IN_APP")
    private DeliveryChannel channel;

    @Schema(description = "Priority level (LOW, NORMAL, HIGH, CRITICAL)", example = "CRITICAL")
    private String priority;

    @Schema(description = "Related entity type", example = "BLOOD_REQUEST")
    private String relatedEntityType;

    @Schema(description = "Related entity ID", example = "10")
    private Long relatedEntityId;

    @Schema(description = "Action URL redirect link", example = "/hospital/requests")
    private String actionUrl;

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public DeliveryChannel getChannel() {
        return channel;
    }

    public void setChannel(DeliveryChannel channel) {
        this.channel = channel;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}
