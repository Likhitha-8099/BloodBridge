package com.bloodbridge.notification;

import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Standardized payload for cross-channel notification dispatching.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload {

    private Long emergencyRequestId;
    private User recipientUser;
    private DonorProfile recipientDonor;
    private Hospital hospital;
    private BloodRequest bloodRequest;

    private String recipientEmail;
    private String recipientPhone;
    private String recipientFcmToken;

    private String title;
    private String message;
    private NotificationType notificationType;
    private String priority;
    private String actionUrl;

    private Map<String, Object> extraData;
}
