package com.bloodbridge.service;

import com.bloodbridge.dto.EmergencyMailDto;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.enums.Role;
import com.bloodbridge.enums.UrgencyLevel;
import com.bloodbridge.repository.NotificationRepository;
import com.bloodbridge.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite verifying Module 1: Matched Donor Email Notification Dispatch.
 * Verifies that when a hospital creates a request and donors are matched:
 * 1. Dashboard notification is created and saved.
 * 2. Matched donor receives the emergency alert HTML email.
 * 3. Email contains correct blood group, hospital name, units, urgency, and loginUrl.
 * 4. Email failures do not fail the request or throw unhandled exceptions.
 */
@ExtendWith(MockitoExtension.class)
class MatchedDonorEmailNotificationTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RealtimeService realtimeService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Hospital hospital;
    private DonorProfile matchedDonor;
    private User donorUser;
    private BloodRequest bloodRequest;

    @BeforeEach
    void setUp() {
        hospital = Hospital.builder()
                .id(1L)
                .hospitalName("Apollo Emergency Care")
                .address("100 Bannerghatta Road")
                .city("Bangalore")
                .state("Karnataka")
                .build();

        donorUser = User.builder()
                .id(10L)
                .email("priya.donor@example.com")
                .fullName("Priya Sharma")
                .role(Role.DONOR)
                .active(true)
                .build();

        matchedDonor = DonorProfile.builder()
                .id(50L)
                .user(donorUser)
                .email("priya.donor@example.com")
                .bloodGroup(BloodGroup.O_POSITIVE)
                .city("Bangalore")
                .state("Karnataka")
                .availableForDonation(true)
                .build();

        bloodRequest = BloodRequest.builder()
                .id(300L)
                .hospital(hospital)
                .bloodGroupNeeded(BloodGroup.O_POSITIVE)
                .unitsRequired(2)
                .urgencyLevel(UrgencyLevel.CRITICAL)
                .requiredByDate(LocalDate.now().plusDays(1))
                .reason("Trauma Unit Surgery")
                .build();
    }

    @Test
    @DisplayName("Should create dashboard notification and dispatch emergency email to matched donor")
    void testNotifyDonor_DispatchesBothDashboardNotificationAndEmail() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyDonor(
                matchedDonor,
                "Emergency O+ blood needed at Apollo Emergency Care",
                "Urgent requirement for 2 units of O+ blood at Apollo Emergency Care.",
                NotificationType.EMERGENCY_BLOOD_REQUEST,
                "/donor/requests",
                bloodRequest,
                hospital
        );

        // 1. Verify dashboard in-app notification saved
        verify(notificationRepository, times(1)).save(any(Notification.class));

        // 2. Verify emergency email dispatched to matched donor
        ArgumentCaptor<EmergencyMailDto> mailCaptor = ArgumentCaptor.forClass(EmergencyMailDto.class);
        verify(emailService, times(1)).sendEmergencyAlert(mailCaptor.capture());

        EmergencyMailDto sentMail = mailCaptor.getValue();
        assertEquals("priya.donor@example.com", sentMail.getToEmail());
        assertEquals("Priya Sharma", sentMail.getDonorName());
        assertEquals("Apollo Emergency Care", sentMail.getHospitalName());
        assertEquals("O_POSITIVE", sentMail.getBloodGroup());
        assertEquals(2, sentMail.getUnitsRequired());
        assertEquals("CRITICAL", sentMail.getUrgencyLevel());
        assertEquals("Trauma Unit Surgery", sentMail.getReason());
        assertTrue(sentMail.getLoginUrl().contains("/donor/requests"));
    }

    @Test
    @DisplayName("Should handle email dispatch exception gracefully without breaking dashboard notification")
    void testNotifyDonor_EmailFailureDoesNotThrow() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("SMTP Server Unreachable"))
                .when(emailService).sendEmergencyAlert(any(EmergencyMailDto.class));

        assertDoesNotThrow(() -> {
            notificationService.notifyDonor(
                    matchedDonor,
                    "Emergency O+ blood needed at Apollo Emergency Care",
                    "Urgent requirement for 2 units of O+ blood at Apollo Emergency Care.",
                    NotificationType.EMERGENCY_BLOOD_REQUEST,
                    "/donor/requests",
                    bloodRequest,
                    hospital
            );
        });

        // Dashboard notification must still be saved
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}
