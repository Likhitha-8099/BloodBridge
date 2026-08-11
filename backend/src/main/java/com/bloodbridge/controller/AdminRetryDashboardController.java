package com.bloodbridge.controller;

import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.DeliveryAnalyticsDTO;
import com.bloodbridge.dto.response.RetryDashboardItemDTO;
import com.bloodbridge.service.DeliveryAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin Controller for inspecting delivery analytics and the notification retry queue.
 */
@RestController
@RequestMapping({"/api/v1/admin", "/api/admin"})
@RequiredArgsConstructor
public class AdminRetryDashboardController {

    private final DeliveryAnalyticsService deliveryAnalyticsService;

    @GetMapping("/notifications/retries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RetryDashboardItemDTO>>> getNotificationRetries() {
        List<RetryDashboardItemDTO> retries = deliveryAnalyticsService.getRetryQueueItems();
        return ResponseEntity.ok(ApiResponse.success("Retry queue items retrieved successfully", retries));
    }

    @GetMapping("/analytics/delivery")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryAnalyticsDTO>> getDeliveryAnalytics() {
        DeliveryAnalyticsDTO analytics = deliveryAnalyticsService.getDeliveryAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Delivery analytics retrieved successfully", analytics));
    }
}
