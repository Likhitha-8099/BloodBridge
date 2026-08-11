package com.bloodbridge.notification;

import com.bloodbridge.dto.request.NotificationPreferenceRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.NotificationPreferenceResponse;
import com.bloodbridge.entity.NotificationPreference;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationCategory;
import com.bloodbridge.enums.Role;
import com.bloodbridge.repository.NotificationPreferenceRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.impl.NotificationPreferenceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationPreferenceTest {

    @Mock private NotificationPreferenceRepository preferenceRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private NotificationPreferenceServiceImpl preferenceService;

    private User testUser;
    private NotificationPreference testPref;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("donor.pref@test.com")
                .role(Role.DONOR)
                .build();

        testPref = NotificationPreference.builder()
                .id(5L)
                .user(testUser)
                .emailEnabled(true)
                .pushEnabled(true)
                .webSocketEnabled(true)
                .emergencyAlertsEnabled(true)
                .rewardNotificationsEnabled(true)
                .reminderNotificationsEnabled(true)
                .adminMessagesEnabled(true)
                .quietHoursEnabled(false)
                .timezone("UTC")
                .build();
    }

    @Test
    @DisplayName("Should create default preferences if none exist")
    void testGetOrCreateDefaultPreference() {
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any())).thenReturn(testPref);

        NotificationPreference res = preferenceService.getOrCreateDefaultPreference(testUser);

        assertNotNull(res);
        assertTrue(res.getEmergencyAlertsEnabled());
        verify(preferenceRepository).save(any());
    }

    @Test
    @DisplayName("Should update user preferences while enforcing Emergency Alerts ON")
    void testUpdatePreferencesEnforcesEmergencyAlerts() {
        when(userRepository.findByEmail("donor.pref@test.com")).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPref));
        when(preferenceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationPreferenceRequest req = NotificationPreferenceRequest.builder()
                .emailEnabled(false)
                .emergencyAlertsEnabled(false) // Try disabling emergency alerts
                .quietHoursEnabled(true)
                .quietHoursStart("23:00")
                .quietHoursEnd("06:00")
                .build();

        ApiResponse<NotificationPreferenceResponse> response = preferenceService.updatePreferences("donor.pref@test.com", req);

        assertNotNull(response);
        assertTrue(response.getData().getEmergencyAlertsEnabled(), "Emergency alerts must remain TRUE always");
        assertFalse(response.getData().getEmailEnabled());
        assertTrue(response.getData().getQuietHoursEnabled());
    }

    @Test
    @DisplayName("Should correctly evaluate channel and category toggles")
    void testChannelAndCategoryToggles() {
        testPref.setEmailEnabled(false);
        testPref.setRewardNotificationsEnabled(false);

        assertFalse(preferenceService.isChannelEnabled(testPref, DeliveryChannel.EMAIL));
        assertTrue(preferenceService.isChannelEnabled(testPref, DeliveryChannel.PUSH));

        assertFalse(preferenceService.isCategoryEnabled(testPref, NotificationCategory.REWARD));
        assertTrue(preferenceService.isCategoryEnabled(testPref, NotificationCategory.EMERGENCY), "EMERGENCY category must always be enabled");
    }
}
