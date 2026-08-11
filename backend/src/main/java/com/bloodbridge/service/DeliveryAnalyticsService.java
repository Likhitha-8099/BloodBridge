package com.bloodbridge.service;

import com.bloodbridge.dto.response.DeliveryAnalyticsDTO;
import com.bloodbridge.dto.response.RetryDashboardItemDTO;

import java.util.List;

/**
 * Service interface for delivery analytics and retry queue inspection.
 */
public interface DeliveryAnalyticsService {

    DeliveryAnalyticsDTO getDeliveryAnalytics();

    List<RetryDashboardItemDTO> getRetryQueueItems();
}
