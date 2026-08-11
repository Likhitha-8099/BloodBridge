package com.bloodbridge.service;

import com.bloodbridge.dto.request.SendNotificationRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.NotificationResponse;
import com.bloodbridge.entity.Notification;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationStatus;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.enums.Role;
import com.bloodbridge.mapper.NotificationMapper;
import com.bloodbridge.provider.NotificationProvider;
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.EmailNotificationRepository;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.NotificationRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link NotificationServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Spy
    private List<NotificationProvider> notificationProviders = new ArrayList<>();

    @Mock
    private AuditLoggerService auditLoggerService;

    @Mock
    private RealtimeService realtimeService;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailNotificationRepository emailNotificationRepository;

    @Mock
    private DonorMatchingService donorMatchingService;

    @Mock
    private Executor emergencyEmailExecutor;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User recipientUser;
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
    }

    @Test
    void sendNotification_Success() {
        SendNotificationRequest sendRequest = SendNotificationRequest.builder()
                .recipientUserId(1L)
                .title("New Blood Request")
                .message("Details here.")
                .type(NotificationType.BLOOD_REQUEST_CREATED)
                .channel(DeliveryChannel.IN_APP)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(recipientUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

        ApiResponse<NotificationResponse> response = notificationService.sendNotification(sendRequest);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("Match Alert", response.getData().getTitle());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void markAsRead_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(recipientUser));
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(
                NotificationResponse.builder().id(100L).readStatus(true).build()
        );

        ApiResponse<NotificationResponse> response = notificationService.markAsRead("john@example.com", 100L);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertTrue(response.getData().getReadStatus());
        assertTrue(notification.getReadStatus());
    }
}
