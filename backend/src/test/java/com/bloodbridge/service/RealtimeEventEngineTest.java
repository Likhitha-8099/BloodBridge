package com.bloodbridge.service;

import com.bloodbridge.dto.RealtimeEventDTO;
import com.bloodbridge.enums.RealtimeEventType;
import com.bloodbridge.service.impl.RealtimeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit & Resilience Tests for {@link RealtimeServiceImpl} and Backend Real-Time Event Engine.
 */
@ExtendWith(MockitoExtension.class)
class RealtimeEventEngineTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RealtimeServiceImpl realtimeService;

    private RealtimeEventDTO requestCreatedEvent;
    private RealtimeEventDTO donorAcceptedEvent;
    private RealtimeEventDTO donorRejectedEvent;

    @BeforeEach
    void setUp() {
        requestCreatedEvent = RealtimeEventDTO.builder()
                .eventType(RealtimeEventType.EMERGENCY_REQUEST_CREATED)
                .requestId(101L)
                .hospitalId(5L)
                .hospitalName("City General Hospital")
                .bloodGroup("B_POSITIVE")
                .status("CREATED")
                .timestamp(LocalDateTime.now())
                .build();

        donorAcceptedEvent = RealtimeEventDTO.builder()
                .eventType(RealtimeEventType.DONOR_ACCEPTED_REQUEST)
                .requestId(101L)
                .matchedDonorId(50L)
                .donorId(12L)
                .donorName("Jane Donor")
                .hospitalId(5L)
                .hospitalName("City General Hospital")
                .bloodGroup("B_POSITIVE")
                .status("ACCEPTED")
                .timestamp(LocalDateTime.now())
                .build();

        donorRejectedEvent = RealtimeEventDTO.builder()
                .eventType(RealtimeEventType.DONOR_REJECTED_REQUEST)
                .requestId(101L)
                .matchedDonorId(51L)
                .donorId(14L)
                .donorName("Bob Donor")
                .hospitalId(5L)
                .hospitalName("City General Hospital")
                .bloodGroup("B_POSITIVE")
                .status("REJECTED")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("1. EMERGENCY_REQUEST_CREATED: Publishes event to /topic/emergency-events, request, and hospital topics")
    void testPublishEmergencyRequestCreatedEvent() {
        realtimeService.publishEmergencyEvent(requestCreatedEvent);

        verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/emergency-events"), eq(requestCreatedEvent));
        verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/emergency-events/request/101"), eq(requestCreatedEvent));
        verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/emergency-events/hospital/5"), eq(requestCreatedEvent));
    }

    @Test
    @DisplayName("2. DONOR_ACCEPTED_REQUEST: Publishes event with full DTO to donor and hospital emergency topics")
    void testPublishDonorAcceptedRequestEvent() {
        realtimeService.publishEmergencyEvent(donorAcceptedEvent);

        ArgumentCaptor<RealtimeEventDTO> captor = ArgumentCaptor.forClass(RealtimeEventDTO.class);
        verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/emergency-events"), captor.capture());

        RealtimeEventDTO captured = captor.getValue();
        assertThat(captured.getEventType()).isEqualTo(RealtimeEventType.DONOR_ACCEPTED_REQUEST);
        assertThat(captured.getRequestId()).isEqualTo(101L);
        assertThat(captured.getDonorId()).isEqualTo(12L);
        assertThat(captured.getStatus()).isEqualTo("ACCEPTED");
    }

    @Test
    @DisplayName("3. DONOR_REJECTED_REQUEST: Publishes event with full DTO to donor and hospital emergency topics")
    void testPublishDonorRejectedRequestEvent() {
        realtimeService.publishEmergencyEvent(donorRejectedEvent);

        ArgumentCaptor<RealtimeEventDTO> captor = ArgumentCaptor.forClass(RealtimeEventDTO.class);
        verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/emergency-events"), captor.capture());

        RealtimeEventDTO captured = captor.getValue();
        assertThat(captured.getEventType()).isEqualTo(RealtimeEventType.DONOR_REJECTED_REQUEST);
        assertThat(captured.getRequestId()).isEqualTo(101L);
        assertThat(captured.getDonorId()).isEqualTo(14L);
        assertThat(captured.getStatus()).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("4. Resilience Check: STOMP Messaging failure does NOT throw exception or crash execution")
    void testPublishEventFailureResilience() {
        doThrow(new RuntimeException("WebSocket Connection Interrupted"))
                .when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        assertDoesNotThrow(() -> realtimeService.publishEmergencyEvent(donorAcceptedEvent));
    }
}
