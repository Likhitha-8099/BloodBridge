package com.bloodbridge.service;

import com.bloodbridge.dto.NotificationCreateRequest;
import com.bloodbridge.dto.NotificationResponse;
import com.bloodbridge.dto.NotificationStatisticsResponse;
import com.bloodbridge.dto.NotificationSummaryResponse;
import com.bloodbridge.entity.Notification;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.NotificationType;

import java.util.List;

/**
 * Service interface defining workflows for managing notifications.
 */
public interface NotificationService {

    /**
     * Creates and stores an in-app notification record.
     *
     * @param request the notification creation details
     * @return the created notification details
     */
    NotificationResponse createNotification(NotificationCreateRequest request);

    /**
     * Sends/processes an existing notification (e.g. queues or delivers mail).
     *
     * @param id the notification ID
     * @return the updated notification details
     */
    NotificationResponse sendNotification(Long id);

    /**
     * Marks a specific notification as read.
     *
     * @param id the notification ID
     * @return the updated notification details
     */
    NotificationResponse markAsRead(Long id);

    /**
     * Retrieves all notifications sent to the currently authenticated user.
     *
     * @return a list of notification summaries
     */
    List<NotificationSummaryResponse> getMyNotifications();

    /**
     * Retrieves unread notifications sent to the currently authenticated user.
     *
     * @return a list of unread notification summaries
     */
    List<NotificationSummaryResponse> getUnreadNotifications();

    /**
     * Retrieves a notification by its ID. Restricted to recipient user or ADMIN.
     *
     * @param id the notification ID
     * @return detailed notification response
     */
    NotificationResponse getNotificationById(Long id);

    /**
     * Dispatches the notification via email and updates status in the database.
     *
     * @param notification the notification entity
     */
    void sendEmailNotification(Notification notification);

    /**
     * High-level entry point to create both an In-App notification and dispatch an Email alert.
     *
     * @param recipient the recipient user entity
     * @param title     the title
     * @param message   the notification body message
     * @param type      the notification type enum
     */
    void triggerNotificationEvent(User recipient, String title, String message, NotificationType type);

    /**
     * Retrieves overall aggregate notification metrics.
     *
     * @return stats details
     */
    NotificationStatisticsResponse getNotificationStatistics();
}
