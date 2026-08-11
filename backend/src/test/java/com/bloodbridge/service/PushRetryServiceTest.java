package com.bloodbridge.service;

import com.bloodbridge.repository.DeviceTokenRepository;
import com.bloodbridge.service.impl.PushRetryServiceImpl;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PushRetryServiceImpl} handling transient errors and backoff logic.
 * Phase 3B.2 — Enterprise Firebase Push Notification Delivery Engine.
 */
@ExtendWith(MockitoExtension.class)
class PushRetryServiceTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @InjectMocks
    private PushRetryServiceImpl pushRetryService;

    @Test
    void isTransientError_IdentifiesTransientErrorCodes() {
        assertTrue(pushRetryService.isTransientError("UNAVAILABLE"));
        assertTrue(pushRetryService.isTransientError("INTERNAL"));
        assertTrue(pushRetryService.isTransientError("DEADLINE_EXCEEDED"));
        assertFalse(pushRetryService.isTransientError("INVALID_ARGUMENT"));
        assertFalse(pushRetryService.isTransientError("UNREGISTERED"));
    }

    @Test
    void sendWithRetry_FirebaseNotInitialized_ReturnsNull() {
        Message message = Message.builder().setToken("test-token").build();
        String result = pushRetryService.sendWithRetry(message, "test-token");
        assertNull(result);
    }

    @Test
    void handleInvalidToken_DeletesTokenFromRepository() {
        doNothing().when(deviceTokenRepository).deleteByFcmToken("bad-token");

        pushRetryService.handleInvalidToken("bad-token", "UNREGISTERED");

        verify(deviceTokenRepository, times(1)).deleteByFcmToken("bad-token");
    }
}
