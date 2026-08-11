package com.bloodbridge.notification;

import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.NotificationResponse;
import com.bloodbridge.entity.Notification;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.*;
import com.bloodbridge.mapper.NotificationMapper;
import com.bloodbridge.provider.NotificationProvider;
import com.bloodbridge.repository.EmailNotificationRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.NotificationRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.AuditLoggerService;
import com.bloodbridge.service.DonorMatchingService;
import com.bloodbridge.service.EmailService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationCenterTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private HospitalRepository hospitalRepository;
    @Mock private EmailNotificationRepository emailNotificationRepository;
    @Mock private DonorMatchingService donorMatchingService;
    @Mock private NotificationMapper notificationMapper;
    @Mock private List<NotificationProvider> notificationProviders;
    @Mock private AuditLoggerService auditLoggerService;
    @Mock private RealtimeService realtimeService;
    @Mock private EmailService emailService;
    @Mock private NotificationPreferenceService preferenceService;
    @Mock private QuietHoursService quietHoursService;
    @Mock private com.bloodbridge.notification.NotificationOrchestrator notificationOrchestrator;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;
    private Notification testNotif;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("john.center@test.com")
                .fullName("John Center")
                .role(Role.DONOR)
                .build();

        testNotif = Notification.builder()
                .id(10L)
                .recipientUser(testUser)
                .title("Emergency Request")
                .message("Urgent B+ blood required")
                .notificationType(NotificationType.EMERGENCY_BLOOD_REQUEST)
                .category(NotificationCategory.EMERGENCY)
                .deliveryChannel(DeliveryChannel.IN_APP)
                .priority("HIGH")
                .priorityEnum(NotificationPriority.HIGH)
                .status(NotificationStatus.SENT)
                .readStatus(false)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should retrieve paginated notifications with filters")
    void testGetNotificationsPaginated() {
        when(userRepository.findByEmail("john.center@test.com")).thenReturn(Optional.of(testUser));
        Page<Notification> page = new PageImpl<>(List.of(testNotif));
        when(notificationRepository.findUserNotificationsFiltered(eq(1L), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(notificationMapper.toResponse(any())).thenReturn(NotificationResponse.builder().id(10L).title("Emergency Request").build());
        when(notificationRepository.countUnreadByRecipientUserId(1L)).thenReturn(1L);

        ApiResponse<Map<String, Object>> result = notificationService.getNotificationsPaginated(
                "john.center@test.com", 0, 10, "EMERGENCY", "HIGH", false, null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getData().get("totalElements"));
        verify(notificationRepository).findUserNotificationsFiltered(eq(1L), eq(NotificationCategory.EMERGENCY), eq(NotificationPriority.HIGH), eq("HIGH"), eq(false), any());
    }

    @Test
    @DisplayName("Should mark notification as read and publish unread count")
    void testMarkAsRead() {
        when(userRepository.findByEmail("john.center@test.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(testNotif));
        when(notificationRepository.save(any())).thenReturn(testNotif);
        when(notificationRepository.countUnreadByRecipientUserId(1L)).thenReturn(0L);

        ApiResponse<NotificationResponse> response = notificationService.markAsRead("john.center@test.com", 10L);

        assertNotNull(response);
        assertTrue(testNotif.getReadStatus());
        verify(realtimeService).publishUnreadCount(1L, 0L);
    }

    @Test
    @DisplayName("Should mark all notifications as read")
    void testMarkAllAsRead() {
        when(userRepository.findByEmail("john.center@test.com")).thenReturn(Optional.of(testUser));

        ApiResponse<String> response = notificationService.markAllAsRead("john.center@test.com");

        assertNotNull(response);
        verify(notificationRepository).markAllAsReadForUser(eq(1L), any());
        verify(realtimeService).publishUnreadCount(1L, 0L);
    }

    @Test
    @DisplayName("Should soft delete notification and recalculate unread count")
    void testDeleteNotification() {
        when(userRepository.findByEmail("john.center@test.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(testNotif));
        when(notificationRepository.countUnreadByRecipientUserId(1L)).thenReturn(0L);

        ApiResponse<String> response = notificationService.deleteNotification("john.center@test.com", 10L);

        assertNotNull(response);
        assertTrue(testNotif.getDeleted());
        verify(realtimeService).publishUnreadCount(1L, 0L);
    }

    @Test
    @DisplayName("Should reject unauthorized deletion attempt")
    void testUnauthorizedDelete() {
        User anotherUser = User.builder().id(99L).email("hacker@test.com").build();
        when(userRepository.findByEmail("hacker@test.com")).thenReturn(Optional.of(anotherUser));
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(testNotif));

        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.deleteNotification("hacker@test.com", 10L)
        );
    }
}
