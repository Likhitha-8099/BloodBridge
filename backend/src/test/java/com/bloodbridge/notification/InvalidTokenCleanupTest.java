package com.bloodbridge.notification;

import com.bloodbridge.repository.DeviceTokenRepository;
import com.bloodbridge.service.impl.PushRetryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit tests for Automatic Invalid Token Cleanup logic.
 * Phase 3B.2 — Enterprise Firebase Push Notification Delivery Engine.
 */
@ExtendWith(MockitoExtension.class)
class InvalidTokenCleanupTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @InjectMocks
    private PushRetryServiceImpl pushRetryService;

    @Test
    void cleanupUnregisteredToken_Success() {
        String unregisteredToken = "unregistered-fcm-token-xyz";

        pushRetryService.handleInvalidToken(unregisteredToken, "UNREGISTERED");

        verify(deviceTokenRepository, times(1)).deleteByFcmToken(unregisteredToken);
    }

    @Test
    void cleanupInvalidArgumentToken_Success() {
        String invalidToken = "invalid-argument-token-abc";

        pushRetryService.handleInvalidToken(invalidToken, "INVALID_ARGUMENT");

        verify(deviceTokenRepository, times(1)).deleteByFcmToken(invalidToken);
    }

    @Test
    void cleanupBlankToken_DoesNothing() {
        pushRetryService.handleInvalidToken("", "UNREGISTERED");

        verify(deviceTokenRepository, never()).deleteByFcmToken(any());
    }
}
