package com.bloodbridge.notification;

import com.bloodbridge.dto.request.SendNotificationRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.NotificationCountResponse;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnreadCounterTest {

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
        testUser = User.builder().id(1L).email("unread.test@bloodbridge.org").role(Role.DONOR).build();
        testNotif = Notification.builder().id(1L).recipientUser(testUser).readStatus(false).status(NotificationStatus.SENT).build();
    }

    @Test
    @DisplayName("Should return accurate unread count and total metrics")
    void testGetNotificationCount() {
        when(userRepository.findByEmail("unread.test@bloodbridge.org")).thenReturn(Optional.of(testUser));
        when(notificationRepository.countUnreadByRecipientUserId(1L)).thenReturn(3L);
        when(notificationRepository.findUserNotifications(1L)).thenReturn(List.of(testNotif, testNotif, testNotif, testNotif));

        ApiResponse<NotificationCountResponse> response = notificationService.getNotificationCount("unread.test@bloodbridge.org");

        assertNotNull(response);
        assertEquals(3L, response.getData().getUnreadCount());
        assertEquals(4L, response.getData().getTotalCount());
    }

    @Test
    @DisplayName("Should return unread badge count payload for UI")
    void testGetUnreadBadgeCount() {
        when(userRepository.findByEmail("unread.test@bloodbridge.org")).thenReturn(Optional.of(testUser));
        when(notificationRepository.countUnreadByRecipientUserId(1L)).thenReturn(5L);

        ApiResponse<Map<String, Object>> response = notificationService.getUnreadBadgeCount("unread.test@bloodbridge.org");

        assertNotNull(response);
        assertEquals(5L, response.getData().get("unreadCount"));
    }

    @Test
    @DisplayName("Should publish real-time unread count on notification creation")
    void testUnreadCountUpdatedOnSend() {
        SendNotificationRequest req = SendNotificationRequest.builder()
                .recipientUserId(1L)
                .title("Test")
                .message("Message")
                .channel(DeliveryChannel.IN_APP)
                .type(NotificationType.SYSTEM_NOTIFICATION)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any())).thenReturn(testNotif);
        when(notificationRepository.countUnreadByRecipientUserId(1L)).thenReturn(4L);

        notificationService.sendNotification(req);

        verify(realtimeService).publishUnreadCount(1L, 4L);
    }
}
