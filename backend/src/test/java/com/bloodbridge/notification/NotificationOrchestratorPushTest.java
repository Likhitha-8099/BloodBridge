package com.bloodbridge.notification;

import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link NotificationOrchestratorImpl} multi-channel parallel delivery.
 * Phase 3B.2 — Enterprise Firebase Push Notification Delivery Engine.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationOrchestratorPushTest {

    @Mock
    private NotificationChannel emailChannel;

    @Mock
    private NotificationChannel webSocketChannel;

    @Mock
    private NotificationChannel pushChannel;

    private NotificationOrchestratorImpl orchestrator;
    private NotificationPayload payload;

    @BeforeEach
    void setUp() {
        when(emailChannel.getChannel()).thenReturn(DeliveryChannel.EMAIL);
        when(emailChannel.isEnabled()).thenReturn(true);

        when(webSocketChannel.getChannel()).thenReturn(DeliveryChannel.IN_APP);
        when(webSocketChannel.isEnabled()).thenReturn(true);

        when(pushChannel.getChannel()).thenReturn(DeliveryChannel.PUSH);
        when(pushChannel.isEnabled()).thenReturn(true);

        orchestrator = new NotificationOrchestratorImpl(List.of(emailChannel, webSocketChannel, pushChannel));

        payload = NotificationPayload.builder()
                .emergencyRequestId(101L)
                .title("Urgent Blood Request")
                .message("O+ Needed")
                .notificationType(NotificationType.EMERGENCY_BLOOD_REQUEST)
                .build();
    }

    @Test
    void dispatchNotification_ParallelExecutionAcrossAllChannels() {
        when(emailChannel.send(any())).thenReturn(true);
        when(webSocketChannel.send(any())).thenReturn(true);
        when(pushChannel.send(any())).thenReturn(true);

        orchestrator.dispatchNotification(payload);

        verify(emailChannel, times(1)).send(payload);
        verify(webSocketChannel, times(1)).send(payload);
        verify(pushChannel, times(1)).send(payload);
    }

    @Test
    void dispatchNotification_ChannelFailureIsIsolated() {
        when(emailChannel.send(any())).thenThrow(new RuntimeException("SMTP Connection Refused"));
        when(webSocketChannel.send(any())).thenReturn(true);
        when(pushChannel.send(any())).thenReturn(true);

        assertDoesNotThrow(() -> orchestrator.dispatchNotification(payload));

        verify(webSocketChannel, times(1)).send(payload);
        verify(pushChannel, times(1)).send(payload);
    }

    @Test
    void getRegisteredChannels_ReturnsConfiguredChannels() {
        assertEquals(3, orchestrator.getRegisteredChannels().size());
    }
}
