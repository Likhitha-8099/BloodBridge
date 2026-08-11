package com.bloodbridge.notification;

import com.bloodbridge.entity.DeviceToken;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.Role;
import com.bloodbridge.notification.channel.FirebaseNotificationChannel;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for multi-device FCM batch dispatching.
 * Phase 3B.2 — Enterprise Firebase Push Notification Delivery Engine.
 */
@ExtendWith(MockitoExtension.class)
class DeviceBatchDispatchTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private PushDeliveryLogRepository pushDeliveryLogRepository;

    @Mock
    private PushRetryService pushRetryService;

    @InjectMocks
    private FirebaseNotificationChannel firebaseNotificationChannel;

    private User multiDeviceUser;
    private DeviceToken desktopToken;
    private DeviceToken mobileToken;
    private NotificationPayload payload;

    @BeforeEach
    void setUp() {
        multiDeviceUser = User.builder()
                .id(99L)
                .fullName("Multi Device Donor")
                .email("multidevice@example.com")
                .role(Role.DONOR)
                .build();

        desktopToken = DeviceToken.builder()
                .id(1L)
                .user(multiDeviceUser)
                .fcmToken("desktop-token-111")
                .browser("Chrome")
                .platform("WEB")
                .isActive(true)
                .build();

        mobileToken = DeviceToken.builder()
                .id(2L)
                .user(multiDeviceUser)
                .fcmToken("mobile-token-222")
                .browser("Mobile Safari")
                .platform("WEB")
                .isActive(true)
                .build();

        payload = NotificationPayload.builder()
                .emergencyRequestId(200L)
                .recipientUser(multiDeviceUser)
                .title("Emergency Request")
                .message("O- Needed")
                .build();
    }

    @Test
    void send_MultipleDevicesPerUser_DispatchesToAllTokens() {
        when(deviceTokenRepository.findAllByUserAndIsActiveTrue(multiDeviceUser))
                .thenReturn(List.of(desktopToken, mobileToken));

        boolean result = firebaseNotificationChannel.send(payload);

        assertTrue(result);
        verify(pushDeliveryLogRepository, times(2)).save(any());
    }
}
