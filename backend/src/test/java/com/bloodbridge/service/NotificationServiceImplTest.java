package com.bloodbridge.service;

import com.bloodbridge.dto.NotificationCreateRequest;
import com.bloodbridge.dto.NotificationResponse;
import com.bloodbridge.dto.NotificationStatisticsResponse;
import com.bloodbridge.entity.Notification;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationStatus;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.enums.Role;
import com.bloodbridge.mapper.NotificationMapper;
import com.bloodbridge.repository.NotificationRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link NotificationServiceImpl}.
 */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User recipientUser;
    private User otherUser;
    private User adminUser;
    private Notification notification;
    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        recipientUser = User.builder()
                .id(1L)
                .fullName("John Recipient")
                .email("john@example.com")
                .role(Role.DONOR)
                .build();

        otherUser = User.builder()
                .id(2L)
                .fullName("Other User")
                .email("other@example.com")
                .role(Role.PATIENT)
                .build();

        adminUser = User.builder()
                .id(3L)
                .fullName("System Admin")
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        notification = Notification.builder()
                .id(100L)
                .recipientUser(recipientUser)
                .title("Match Alert")
                .message("You have a match request.")
                .notificationType(NotificationType.DONOR_MATCHED)
                .deliveryChannel(DeliveryChannel.IN_APP)
                .status(NotificationStatus.SENT)
                .readStatus(false)
                .build();

        notificationResponse = NotificationResponse.builder()
                .id(100L)
                .recipientUserId(1L)
                .title("Match Alert")
                .message("You have a match request.")
                .notificationType(NotificationType.DONOR_MATCHED)
                .deliveryChannel(DeliveryChannel.IN_APP)
                .status(NotificationStatus.SENT)
                .readStatus(false)
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    private void mockSecurityContext(String email, User userContext) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userContext));
    }

    @Test
    void createNotification_Success() {
        NotificationCreateRequest createRequest = NotificationCreateRequest.builder()
                .recipientUserId(1L)
                .title("New Blood Request")
                .message("Details here.")
                .notificationType(NotificationType.BLOOD_REQUEST_CREATED)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(recipientUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

        NotificationResponse response = notificationService.createNotification(createRequest);

        assertNotNull(response);
        assertEquals("Match Alert", response.getTitle());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void markAsRead_Success() {
        mockSecurityContext("john@example.com", recipientUser);
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(
                NotificationResponse.builder().id(100L).readStatus(true).build()
        );

        NotificationResponse response = notificationService.markAsRead(100L);

        assertNotNull(response);
        assertTrue(response.getReadStatus());
        assertTrue(notification.getReadStatus());
    }

    @Test
    void markAsRead_ThrowsAccessDeniedException_WhenNotRecipient() {
        mockSecurityContext("other@example.com", otherUser);
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));

        assertThrows(AccessDeniedException.class, () -> notificationService.markAsRead(100L));
    }

    @Test
    void getNotificationById_Recipient_Success() {
        mockSecurityContext("john@example.com", recipientUser);
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        NotificationResponse response = notificationService.getNotificationById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void getNotificationById_Admin_Success() {
        mockSecurityContext("admin@example.com", adminUser);
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        NotificationResponse response = notificationService.getNotificationById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void getNotificationById_ThrowsAccessDeniedException_WhenUnprivilegedUser() {
        mockSecurityContext("other@example.com", otherUser);
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));

        assertThrows(AccessDeniedException.class, () -> notificationService.getNotificationById(100L));
    }

    @Test
    void getNotificationStatistics_Success() {
        when(notificationRepository.count()).thenReturn(10L);
        when(notificationRepository.countByReadStatus(true)).thenReturn(4L);
        when(notificationRepository.countByReadStatus(false)).thenReturn(6L);
        when(notificationRepository.countByStatus(NotificationStatus.FAILED)).thenReturn(1L);
        when(notificationRepository.countByStatus(NotificationStatus.SENT)).thenReturn(9L);

        NotificationStatisticsResponse stats = notificationService.getNotificationStatistics();

        assertNotNull(stats);
        assertEquals(10L, stats.getTotalNotifications());
        assertEquals(4L, stats.getReadNotifications());
        assertEquals(6L, stats.getUnreadNotifications());
        assertEquals(1L, stats.getFailedNotifications());
        assertEquals(9L, stats.getSentNotifications());
    }
}
