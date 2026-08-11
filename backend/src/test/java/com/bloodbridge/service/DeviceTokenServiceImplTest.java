package com.bloodbridge.service;

import com.bloodbridge.dto.request.RegisterDeviceTokenRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.DeviceTokenResponse;
import com.bloodbridge.entity.DeviceToken;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.Role;
import com.bloodbridge.repository.DeviceTokenRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.DeviceTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DeviceTokenServiceImpl}.
 * Phase 3B.1 — Device Registration module tests.
 */
@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceImplTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeviceTokenServiceImpl deviceTokenService;

    private User user;
    private DeviceToken deviceToken;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .fullName("Test Donor")
                .email("donor@example.com")
                .role(Role.DONOR)
                .build();

        deviceToken = DeviceToken.builder()
                .id(10L)
                .user(user)
                .fcmToken("sample-fcm-token-12345")
                .platform("WEB")
                .browser("Chrome")
                .deviceName("Chrome / Windows")
                .isActive(true)
                .lastSeen(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void registerToken_success() {
        RegisterDeviceTokenRequest request = RegisterDeviceTokenRequest.builder()
                .token("sample-fcm-token-12345")
                .platform("WEB")
                .browser("Chrome")
                .build();

        when(userRepository.findByEmail("donor@example.com")).thenReturn(Optional.of(user));
        when(deviceTokenRepository.findByFcmToken("sample-fcm-token-12345")).thenReturn(Optional.empty());
        when(deviceTokenRepository.save(any(DeviceToken.class))).thenReturn(deviceToken);

        ApiResponse<DeviceTokenResponse> response = deviceTokenService.registerToken("donor@example.com", request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(10L, response.getData().getId());
        assertTrue(response.getData().getRegistered());
        verify(deviceTokenRepository, times(1)).save(any(DeviceToken.class));
    }

    @Test
    void registerDuplicateToken_updates() {
        RegisterDeviceTokenRequest request = RegisterDeviceTokenRequest.builder()
                .token("sample-fcm-token-12345")
                .platform("WEB")
                .browser("Firefox")
                .build();

        when(userRepository.findByEmail("donor@example.com")).thenReturn(Optional.of(user));
        when(deviceTokenRepository.findByFcmToken("sample-fcm-token-12345")).thenReturn(Optional.of(deviceToken));
        when(deviceTokenRepository.save(any(DeviceToken.class))).thenReturn(deviceToken);

        ApiResponse<DeviceTokenResponse> response = deviceTokenService.registerToken("donor@example.com", request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertFalse(response.getData().getRegistered()); // false indicates update, not new registration
        assertEquals("Firefox", deviceToken.getBrowser());
        verify(deviceTokenRepository, times(1)).save(deviceToken);
    }

    @Test
    void multipleDevices_sameUser() {
        DeviceToken secondToken = DeviceToken.builder()
                .id(11L)
                .user(user)
                .fcmToken("sample-fcm-token-67890")
                .platform("WEB")
                .browser("Safari")
                .isActive(true)
                .build();

        when(userRepository.findByEmail("donor@example.com")).thenReturn(Optional.of(user));
        when(deviceTokenRepository.findAllByUserAndIsActiveTrue(user)).thenReturn(List.of(deviceToken, secondToken));

        ApiResponse<List<DeviceTokenResponse>> response = deviceTokenService.getUserActiveTokens("donor@example.com");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(2, response.getData().size());
    }

    @Test
    void removeToken_success() {
        when(userRepository.findByEmail("donor@example.com")).thenReturn(Optional.of(user));
        when(deviceTokenRepository.findByFcmTokenAndUser("sample-fcm-token-12345", user)).thenReturn(Optional.of(deviceToken));
        doNothing().when(deviceTokenRepository).deleteByFcmToken("sample-fcm-token-12345");

        ApiResponse<Void> response = deviceTokenService.removeToken("donor@example.com", "sample-fcm-token-12345");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(deviceTokenRepository, times(1)).deleteByFcmToken("sample-fcm-token-12345");
    }

    @Test
    void refreshToken_success() {
        when(userRepository.findByEmail("donor@example.com")).thenReturn(Optional.of(user));
        when(deviceTokenRepository.findByFcmTokenAndUser("old-token", user)).thenReturn(Optional.of(deviceToken));
        when(deviceTokenRepository.save(any(DeviceToken.class))).thenReturn(deviceToken);

        ApiResponse<DeviceTokenResponse> response = deviceTokenService.refreshToken("donor@example.com", "old-token", "new-token");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("new-token", deviceToken.getFcmToken());
        verify(deviceTokenRepository, times(1)).save(deviceToken);
    }
}
