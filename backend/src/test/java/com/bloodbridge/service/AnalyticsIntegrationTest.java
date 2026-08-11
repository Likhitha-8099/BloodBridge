package com.bloodbridge.service;

import com.bloodbridge.dto.response.PushAnalyticsResponse;
import com.bloodbridge.enums.EmailDeliveryStatus;

import com.bloodbridge.repository.EmailNotificationRepository;
import com.bloodbridge.repository.NotificationRepository;
import com.bloodbridge.repository.PushDeliveryLogRepository;
import com.bloodbridge.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Push Notification Analytics in {@link DashboardServiceImpl}.
 * Phase 3B.2 — Enterprise Firebase Push Notification Delivery Engine.
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsIntegrationTest {

    @Mock
    private PushDeliveryLogRepository pushDeliveryLogRepository;

    @Mock
    private EmailNotificationRepository emailNotificationRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getPushNotificationAnalytics_ReturnsAggregatedMetrics() {
        when(emailNotificationRepository.countByStatus(EmailDeliveryStatus.SENT)).thenReturn(25L);
        when(emailNotificationRepository.countByStatus(EmailDeliveryStatus.FAILED)).thenReturn(2L);
        when(notificationRepository.countByDeliveryChannel(com.bloodbridge.enums.DeliveryChannel.IN_APP)).thenReturn(50L);

        when(pushDeliveryLogRepository.countByStatus("SENT")).thenReturn(100L);
        when(pushDeliveryLogRepository.countByStatus("FAILED")).thenReturn(5L);
        when(pushDeliveryLogRepository.findAverageLatencyMs()).thenReturn(150.5);
        when(pushDeliveryLogRepository.findTotalRetryCount()).thenReturn(3L);
        when(pushDeliveryLogRepository.countDistinctInvalidTokens()).thenReturn(4L);

        Object[] failureRow = new Object[]{"UNREGISTERED", 4L};
        List<Object[]> topFailures = new java.util.ArrayList<>();
        topFailures.add(failureRow);
        when(pushDeliveryLogRepository.findTopFailureReasons()).thenReturn(topFailures);

        PushAnalyticsResponse response = dashboardService.getPushNotificationAnalytics();

        assertNotNull(response);
        assertEquals(25L, response.getEmailsSent());
        assertEquals(2L, response.getEmailsFailed());
        assertEquals(50L, response.getWebSocketDelivered());
        assertEquals(100L, response.getPushSent());
        assertEquals(5L, response.getPushFailed());
        assertEquals(95.2, response.getPushSuccessPercentage());
        assertEquals(150.5, response.getAveragePushLatencyMs());
        assertEquals(3L, response.getRetryCount());
        assertEquals(4L, response.getInvalidTokensRemoved());
        assertTrue(response.getTopFailureReasons().containsKey("UNREGISTERED"));
    }
}
