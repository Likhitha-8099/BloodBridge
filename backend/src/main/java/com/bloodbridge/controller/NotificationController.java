package com.bloodbridge.controller;

import com.bloodbridge.dto.NotificationResponse;
import com.bloodbridge.dto.NotificationStatisticsResponse;
import com.bloodbridge.dto.NotificationSummaryResponse;
import com.bloodbridge.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Retrieves all notifications sent to the currently authenticated user.
     * Accessible by DONOR, PATIENT, HOSPITAL, or ADMIN.
     *
     * @return list of notification summaries
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DONOR', 'PATIENT', 'HOSPITAL', 'ADMIN')")
    public ResponseEntity<List<NotificationSummaryResponse>> getMyNotifications() {
        List<NotificationSummaryResponse> response = notificationService.getMyNotifications();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves unread notifications sent to the currently authenticated user.
     * Accessible by DONOR, PATIENT, HOSPITAL, or ADMIN.
     *
     * @return list of unread notification summaries
     */
    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('DONOR', 'PATIENT', 'HOSPITAL', 'ADMIN')")
    public ResponseEntity<List<NotificationSummaryResponse>> getUnreadNotifications() {
        List<NotificationSummaryResponse> response = notificationService.getUnreadNotifications();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves detailed notification details by ID. Restricted to recipient user or ADMIN.
     *
     * @param id the notification ID
     * @return detailed notification response
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DONOR', 'PATIENT', 'HOSPITAL', 'ADMIN')")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        NotificationResponse response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Marks a specific notification as read. Restricted to the recipient user.
     *
     * @param id the notification ID
     * @return detailed notification response
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('DONOR', 'PATIENT', 'HOSPITAL', 'ADMIN')")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves global notification system metrics. Restricted to ADMIN role.
     *
     * @return statistics response
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationStatisticsResponse> getNotificationStatistics() {
        NotificationStatisticsResponse response = notificationService.getNotificationStatistics();
        return ResponseEntity.ok(response);
    }
}
