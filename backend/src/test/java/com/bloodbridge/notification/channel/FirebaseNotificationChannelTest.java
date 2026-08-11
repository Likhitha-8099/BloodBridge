package com.bloodbridge.notification.channel;

import com.bloodbridge.entity.DeviceToken;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.enums.Role;
import com.bloodbridge.notification.NotificationPayload;
import com.bloodbridge.repository.DeviceTokenRepository;
import com.bloodbridge.repository.PushDeliveryLogRepository;
import com.bloodbridge.service.PushRetryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FirebaseNotificationChannel}.
 * Phase 3B.2 — Enterprise Firebase Push Notification Delivery Engine.
 */
@ExtendWith(MockitoExtension.class)
class FirebaseNotificationChannelTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private PushDeliveryLogRepository pushDeliveryLogRepository;

    @Mock
    private PushRetryService pushRetryService;

    @InjectMocks
    private FirebaseNotificationChannel firebaseNotificationChannel;

    private User recipientUser;
    private DeviceToken deviceToken;
    private NotificationPayload payload;

    @BeforeEach
    void setUp() {
        recipientUser = User.builder()
                .id(1L)
                .fullName("John Donor")
                .email("john.donor@example.com")
                .role(Role.DONOR)
                .build();

        deviceToken = DeviceToken.builder()
                .id(10L)
                .user(recipientUser)
                .fcmToken("fcm-test-token-12345")
                .platform("WEB")
                .isActive(true)
                .build();

        payload = NotificationPayload.builder()
                .emergencyRequestId(100L)
                .recipientUser(recipientUser)
                .recipientDonor(DonorProfile.builder().id(5L).user(recipientUser).build())
                .title("Emergency Blood Needed")
                .message("Apollo Hospital urgently needs O- blood")
                .notificationType(NotificationType.EMERGENCY_BLOOD_REQUEST)
                .priority("HIGH")
                .extraData(Map.of("distanceKm", 4.5))
                .build();
    }

    @Test
    void getChannel_ReturnsPush() {
        assertEquals(DeliveryChannel.PUSH, firebaseNotificationChannel.getChannel());
    }

    @Test
    void send_NullPayload_ReturnsFalse() {
        assertFalse(firebaseNotificationChannel.send(null));
    }

    @Test
    void send_NoActiveTokens_ReturnsTrueAndSkips() {
        when(deviceTokenRepository.findAllByUserAndIsActiveTrue(recipientUser)).thenReturn(List.of());

        boolean result = firebaseNotificationChannel.send(payload);

        assertTrue(result);
        verify(pushDeliveryLogRepository, never()).save(any());
    }

    @Test
    void send_WithActiveTokens_LogsDeliveryResult() {
        when(deviceTokenRepository.findAllByUserAndIsActiveTrue(recipientUser)).thenReturn(List.of(deviceToken));

        boolean result = firebaseNotificationChannel.send(payload);

        assertTrue(result);
        verify(pushDeliveryLogRepository, times(1)).save(any());
    }
}
