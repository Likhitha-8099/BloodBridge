package com.bloodbridge.service;

import com.bloodbridge.dto.NotificationDTO;
import com.bloodbridge.dto.RealtimeEventDTO;

/**
 * Service interface for publishing real-time WebSocket events across system topics.
 */
public interface RealtimeService {

    void publishAdminDashboardUpdate(Object payload);

    void publishAdminUsersUpdate(Object payload);

    void publishAdminHospitalsUpdate(Object payload);

    void publishAdminNotification(Object payload);

    void publishHospitalUpdate(Long hospitalId, RealtimeEventDTO event);

    void publishDonorUpdate(Long donorId, RealtimeEventDTO event);

    void publishPatientUpdate(Long patientId, RealtimeEventDTO event);

    void publishUserNotification(Long userId, NotificationDTO notification);

    void publishUnreadCount(Long userId, long unreadCount);

    void publishAnalyticsUpdate(Object payload);

    void publishEmergencyEvent(RealtimeEventDTO event);

    void publishGlobalEvent(String destination, Object payload);
}
