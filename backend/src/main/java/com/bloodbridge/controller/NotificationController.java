package com.bloodbridge.controller;

import com.bloodbridge.dto.request.BroadcastAnnouncementRequest;
import com.bloodbridge.dto.request.SendNotificationRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.NotificationCountResponse;
import com.bloodbridge.dto.response.NotificationResponse;
import com.bloodbridge.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Enterprise REST controller for Notification Center endpoints under /api/v1/notifications and /api/notifications.
 */
@RestController
@RequestMapping({"/api/v1/notifications", "/api/notifications"})
@RequiredArgsConstructor
@Tag(name = "Notification & Communication Module", description = "Endpoints for Notification Center, History, Preferences, Unread Counts, and Admin Announcements")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    @Operation(summary = "Get Paginated Notifications", description = "Retrieves notification history for authenticated user with cursor/pagination, category, priority, and read filters.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated notifications retrieved")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotifications(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) Long cursor,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to fetch notifications for user: {} (page: {}, size: {}, category: {}, priority: {})",
                userDetails.getUsername(), page, size, category, priority);
        ApiResponse<Map<String, Object>> response = notificationService.getNotificationsPaginated(
                userDetails.getUsername(), page, size, category, priority, read, cursor);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Unread Count Badge", description = "Returns total unread notification count badge for header/navbar.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unread count retrieved")
    })
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUnreadBadgeCount(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch unread badge count for user: {}", userDetails.getUsername());
        ApiResponse<Map<String, Object>> response = notificationService.getUnreadBadgeCount(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Unread Notifications List", description = "Retrieves list of unread notification items.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unread notifications list retrieved")
    })
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch unread notifications list for user: {}", userDetails.getUsername());
        ApiResponse<List<NotificationResponse>> response = notificationService.getUnreadNotifications(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark Notification as Read", description = "Marks a single notification item as read.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification marked as read")
    })
    @RequestMapping(value = "/{id}/read", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to mark notification ID: {} as read for user: {}", id, userDetails.getUsername());
        ApiResponse<NotificationResponse> response = notificationService.markAsRead(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark All Notifications as Read", description = "Marks all unread notifications for the user as read.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All notifications marked as read")
    })
    @RequestMapping(value = "/read-all", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to mark all notifications as read for user: {}", userDetails.getUsername());
        ApiResponse<String> response = notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Notification", description = "Soft deletes a notification item.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification deleted")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Request to delete notification ID: {} for user: {}", id, userDetails.getUsername());
        ApiResponse<String> response = notificationService.deleteNotification(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Notification Unread & Total Count Metrics", description = "Retrieves unread and total notification counts.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification metrics retrieved")
    })
    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationCountResponse>> getNotificationCount(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Request to fetch notification count metrics for user: {}", userDetails.getUsername());
        ApiResponse<NotificationCountResponse> response = notificationService.getNotificationCount(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Send Custom Notification", description = "Dispatches a notification using target provider strategy.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification sent")
    })
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        log.info("Request to send notification to user ID: {}", request.getRecipientUserId());
        ApiResponse<NotificationResponse> response = notificationService.sendNotification(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Admin: Broadcast System Announcement", description = "Broadcasts a system announcement notification to all registered users.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Broadcast announcement sent")
    })
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> broadcastAnnouncement(
            @Valid @RequestBody BroadcastAnnouncementRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Admin {} broadcasting announcement: {}", userDetails.getUsername(), request.getTitle());
        ApiResponse<String> response = notificationService.broadcastAnnouncement(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
}
