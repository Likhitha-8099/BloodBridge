package com.bloodbridge.dto.request;

import com.bloodbridge.enums.NotificationTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for broadcasting a system announcement by Admin.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Broadcast Announcement Request Payload")
public class BroadcastAnnouncementRequest {

    @NotBlank(message = "Title is required")
    @Schema(description = "Announcement title", example = "System Maintenance Notice")
    private String title;

    @NotBlank(message = "Message is required")
    @Schema(description = "Announcement body", example = "Blood Bridge Network will undergo scheduled maintenance tonight at 02:00 AM UTC.")
    private String message;

    @Schema(description = "Priority level", example = "NORMAL")
    private String priority;

    @Schema(description = "Target audience (ALL, DONOR, PATIENT, HOSPITAL, ADMIN)", example = "ALL")
    private NotificationTarget target;

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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public NotificationTarget getTarget() {
        return target;
    }

    public void setTarget(NotificationTarget target) {
        this.target = target;
    }
}
