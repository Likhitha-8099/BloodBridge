package com.bloodbridge.notification;

import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.notification.channel.EmailNotificationChannel;
import com.bloodbridge.notification.channel.WebSocketNotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationOrchestratorTest {

    private EmailNotificationChannel emailChannel;
    private WebSocketNotificationChannel webSocketChannel;
    private NotificationOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        emailChannel = mock(EmailNotificationChannel.class);
        webSocketChannel = mock(WebSocketNotificationChannel.class);

        when(emailChannel.getChannel()).thenReturn(DeliveryChannel.EMAIL);
        when(emailChannel.isEnabled()).thenReturn(true);

        when(webSocketChannel.getChannel()).thenReturn(DeliveryChannel.IN_APP);
        when(webSocketChannel.isEnabled()).thenReturn(true);

        orchestrator = new NotificationOrchestratorImpl(List.of(emailChannel, webSocketChannel));
    }

    @Test
    void dispatchNotification_RoutesToActiveChannels() {
        NotificationPayload payload = NotificationPayload.builder()
                .emergencyRequestId(100L)
                .recipientEmail("test@example.com")
                .title("Emergency Request")
                .message("Urgent need for O+ blood")
                .build();

        orchestrator.dispatchNotification(payload);

        verify(emailChannel, times(1)).send(payload);
        verify(webSocketChannel, times(1)).send(payload);
    }

    @Test
    void getRegisteredChannels_ReturnsConfiguredStrategyList() {
        List<NotificationChannel> channels = orchestrator.getRegisteredChannels();
        assertEquals(2, channels.size());
    }
}
