package com.bloodbridge.service;

import com.bloodbridge.dto.request.BroadcastAnnouncementRequest;
import com.bloodbridge.dto.request.SendNotificationRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.NotificationCountResponse;
import com.bloodbridge.dto.response.NotificationResponse;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationType;

import java.util.List;
import java.util.Map;

/**
 * Centralized service interface for Automated Notification, Alert & Communication System.
 */
public interface NotificationService {

    ApiResponse<NotificationResponse> sendNotification(SendNotificationRequest request);

    ApiResponse<NotificationResponse> markAsRead(String email, Long id);

    ApiResponse<String> markAllAsRead(String email);

    ApiResponse<List<NotificationResponse>> getMyNotifications(String email);

    ApiResponse<Map<String, Object>> getNotificationsPaginated(String email, Integer page, Integer size, String category, String priority, Boolean read, Long cursor);

    ApiResponse<List<NotificationResponse>> getUnreadNotifications(String email);

    ApiResponse<NotificationCountResponse> getNotificationCount(String email);

    ApiResponse<Map<String, Object>> getUnreadBadgeCount(String email);

    ApiResponse<String> deleteNotification(String email, Long id);

    ApiResponse<String> broadcastAnnouncement(String adminEmail, BroadcastAnnouncementRequest request);

    void retryFailedNotifications();

    void triggerNotificationEvent(User recipient, String title, String message, NotificationType type, DeliveryChannel channel, String priority);

    void triggerNotificationEvent(User recipient, String title, String message, NotificationType type);

    void createDonorAcceptedNotification(DonorProfile donor, Hospital hospital, BloodRequest request);

    ApiResponse<List<NotificationResponse>> getHospitalNotifications(String email);

    void notifyHospital(Hospital hospital, String title, String message, NotificationType type, String actionUrl, BloodRequest bloodRequest, DonorProfile donor);
    void notifyDonor(DonorProfile donor, String title, String message, NotificationType type, String actionUrl, BloodRequest bloodRequest, Hospital hospital);
    void notifyPatient(User patientUser, String title, String message, NotificationType type, String actionUrl, BloodRequest bloodRequest);
    void notifyAdmin(String title, String message, NotificationType type, String actionUrl);
    void notifyNearbyCompatibleDonors(BloodRequest request);
}
