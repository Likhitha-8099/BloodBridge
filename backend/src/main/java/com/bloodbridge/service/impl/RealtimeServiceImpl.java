package com.bloodbridge.service.impl;

import com.bloodbridge.dto.NotificationDTO;
import com.bloodbridge.dto.RealtimeEventDTO;
import com.bloodbridge.service.RealtimeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Enterprise implementation of {@link RealtimeService} using Spring's {@link SimpMessagingTemplate}.
 * Publishes events to role-specific and entity-specific STOMP WebSocket topics.
 */
@Service
@RequiredArgsConstructor
public class RealtimeServiceImpl implements RealtimeService {

    private static final Logger log = LoggerFactory.getLogger(RealtimeServiceImpl.class);

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publishAdminDashboardUpdate(Object payload) {
        publishGlobalEvent("/topic/admin/dashboard", payload);
    }

    @Override
    public void publishAdminUsersUpdate(Object payload) {
        publishGlobalEvent("/topic/admin/users", payload);
    }

    @Override
    public void publishAdminHospitalsUpdate(Object payload) {
        publishGlobalEvent("/topic/admin/hospitals", payload);
    }

    @Override
    public void publishAdminNotification(Object payload) {
        publishGlobalEvent("/topic/admin/notifications", payload);
    }

    @Override
    public void publishHospitalUpdate(Long hospitalId, RealtimeEventDTO event) {
        if (hospitalId == null) return;
        try {
            String topic = "/topic/hospital/" + hospitalId;
            log.info("[REALTIME] Pushing update to hospital topic {}: [{}]", topic, event != null ? event.getEventType() : "EVENT");
            messagingTemplate.convertAndSend(topic, event);
        } catch (Exception e) {
            log.error("[REALTIME-ERROR] Failed to publish hospital WebSocket event to {}: {}", hospitalId, e.getMessage());
        }
    }

    @Override
    public void publishDonorUpdate(Long donorId, RealtimeEventDTO event) {
        if (donorId == null) return;
        try {
            String topic = "/topic/donor/" + donorId;
            log.info("[REALTIME] Pushing update to donor topic {}: [{}]", topic, event != null ? event.getEventType() : "EVENT");
            messagingTemplate.convertAndSend(topic, event);
        } catch (Exception e) {
            log.error("[REALTIME-ERROR] Failed to publish donor WebSocket event to {}: {}", donorId, e.getMessage());
        }
    }

    @Override
    public void publishPatientUpdate(Long patientId, RealtimeEventDTO event) {
        if (patientId == null) return;
        try {
            String topic = "/topic/patient/" + patientId;
            log.info("[REALTIME] Pushing update to patient topic {}: [{}]", topic, event != null ? event.getEventType() : "EVENT");
            messagingTemplate.convertAndSend(topic, event);
        } catch (Exception e) {
            log.error("[REALTIME-ERROR] Failed to publish patient WebSocket event to {}: {}", patientId, e.getMessage());
        }
    }

    @Override
    public void publishUserNotification(Long userId, NotificationDTO notification) {
        if (userId == null || notification == null) return;
        try {
            String topic = "/topic/notifications/" + userId;
            log.info("[REALTIME] Pushing notification to user topic {}: notification #{}", topic, notification.getId());
            messagingTemplate.convertAndSend(topic, notification);
        } catch (Exception e) {
            log.error("[REALTIME-ERROR] Failed to publish notification WebSocket event for user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void publishUnreadCount(Long userId, long unreadCount) {
        if (userId == null) return;
        try {
            String topic = "/topic/notifications/" + userId + "/unread-count";
            log.info("[REALTIME] Pushing unread count update to user topic {}: {}", topic, unreadCount);
            messagingTemplate.convertAndSend(topic, Map.of("unreadCount", unreadCount, "userId", userId));
        } catch (Exception e) {
            log.error("[REALTIME-ERROR] Failed to publish unread count event for user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void publishAnalyticsUpdate(Object payload) {
        publishGlobalEvent("/topic/analytics", payload);
    }

    @Override
    public void publishEmergencyEvent(RealtimeEventDTO event) {
        if (event == null) return;
        try {
            log.info("[REALTIME] Event published: [{}] requestId={}, matchedDonorId={}, donorId={}, hospitalId={}",
                    event.getEventType(), event.getRequestId(), event.getMatchedDonorId(), event.getDonorId(), event.getHospitalId());
            if (event.getEventType() != null) {
                log.info("[REALTIME] {}", event.getEventType().name());
            }
            if (event.getRequestId() != null) {
                log.info("[REALTIME] requestId={}", event.getRequestId());
            }
            if (event.getDonorId() != null) {
                log.info("[REALTIME] donorId={}", event.getDonorId());
            }

            // Publish to general emergency topics
            publishGlobalEvent("/topic/emergency-events", event);

            if (event.getRequestId() != null) {
                publishGlobalEvent("/topic/emergency-events/request/" + event.getRequestId(), event);
            }
            if (event.getHospitalId() != null) {
                publishGlobalEvent("/topic/emergency-events/hospital/" + event.getHospitalId(), event);
                publishHospitalUpdate(event.getHospitalId(), event);
            }
            if (event.getDonorId() != null) {
                publishGlobalEvent("/topic/emergency-events/donor/" + event.getDonorId(), event);
                publishDonorUpdate(event.getDonorId(), event);
            }
        } catch (Exception ex) {
            log.error("[REALTIME-ERROR] Failed to publish emergency event [{}]: {}", event.getEventType(), ex.getMessage());
        }
    }

    @Override
    public void publishGlobalEvent(String destination, Object payload) {
        if (destination == null || destination.isBlank()) return;
        try {
            log.info("[REALTIME] Publishing WebSocket STOMP message to destination: {}", destination);
            messagingTemplate.convertAndSend(destination, payload);
        } catch (Exception e) {
            log.error("[REALTIME-ERROR] Failed to publish WebSocket message to {}: {}", destination, e.getMessage());
        }
    }
}
