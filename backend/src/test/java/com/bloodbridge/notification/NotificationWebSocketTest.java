package com.bloodbridge.notification;

import com.bloodbridge.dto.NotificationDTO;
import com.bloodbridge.dto.request.SendNotificationRequest;
import com.bloodbridge.entity.Notification;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationStatus;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.enums.Role;
import com.bloodbridge.mapper.NotificationMapper;
import com.bloodbridge.provider.NotificationProvider;
import com.bloodbridge.repository.NotificationRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.AuditLoggerService;
import com.bloodbridge.service.NotificationPreferenceService;
import com.bloodbridge.service.QuietHoursService;
import com.bloodbridge.service.RealtimeService;
import com.bloodbridge.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationWebSocketTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationMapper notificationMapper;
    @Mock private List<NotificationProvider> notificationProviders;
    @Mock private AuditLoggerService auditLoggerService;
    @Mock private RealtimeService realtimeService;
    @Mock private NotificationPreferenceService preferenceService;
    @Mock private QuietHoursService quietHoursService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;
    private Notification testNotif;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(7L).email("ws.user@bloodbridge.org").role(Role.DONOR).build();
        testNotif = Notification.builder().id(100L).recipientUser(testUser).status(NotificationStatus.SENT).readStatus(false).build();
    }

    @Test
    @DisplayName("Should publish STOMP notification DTO and unread count over WebSocket")
    void testWebSocketBroadcastOnSend() {
        SendNotificationRequest req = SendNotificationRequest.builder()
                .recipientUserId(7L)
                .title("WebSocket Test")
                .message("WebSocket Event")
                .channel(DeliveryChannel.IN_APP)
                .type(NotificationType.SYSTEM_NOTIFICATION)
                .build();

        when(userRepository.findById(7L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any())).thenReturn(testNotif);
        when(notificationMapper.toDto(testNotif)).thenReturn(NotificationDTO.builder().id(100L).recipientUserId(7L).build());
        when(notificationRepository.countUnreadByRecipientUserId(7L)).thenReturn(1L);

        notificationService.sendNotification(req);

        verify(realtimeService).publishUserNotification(eq(7L), any(NotificationDTO.class));
        verify(realtimeService).publishUnreadCount(eq(7L), eq(1L));
    }

    @Test
    @DisplayName("Should broadcast unread count update when marking notification as read")
    void testWebSocketBroadcastOnMarkRead() {
        when(userRepository.findByEmail("ws.user@bloodbridge.org")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(testNotif));
        when(notificationRepository.save(any())).thenReturn(testNotif);
        when(notificationRepository.countUnreadByRecipientUserId(7L)).thenReturn(0L);

        notificationService.markAsRead("ws.user@bloodbridge.org", 100L);

        verify(realtimeService).publishUnreadCount(eq(7L), eq(0L));
    }

    @Test
    @DisplayName("Should broadcast 0 unread count on mark all read")
    void testWebSocketBroadcastOnMarkAllRead() {
        when(userRepository.findByEmail("ws.user@bloodbridge.org")).thenReturn(Optional.of(testUser));

        notificationService.markAllAsRead("ws.user@bloodbridge.org");

        verify(realtimeService).publishUnreadCount(eq(7L), eq(0L));
    }
}
