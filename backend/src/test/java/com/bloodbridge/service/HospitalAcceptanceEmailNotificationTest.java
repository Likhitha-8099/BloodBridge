package com.bloodbridge.service;

import com.bloodbridge.entity.*;
import com.bloodbridge.enums.BloodGroup;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for Module 2: Hospital Email Notification After Donor Acceptance.
 * Verifies:
 * 1. Hospital in-app notification is saved.
 * 2. Hospital receives email to registered address.
 * 3. Email parameters include Request ID, Hospital Name, Donor Name, Blood Group, Units.
 * 4. Email errors are handled gracefully without aborting the acceptance workflow.
 */
@ExtendWith(MockitoExtension.class)
class HospitalAcceptanceEmailNotificationTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RealtimeService realtimeService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Hospital hospital;
    private User hospitalUser;
    private DonorProfile donor;
    private User donorUser;
    private BloodRequest bloodRequest;

    @BeforeEach
    void setUp() {
        hospitalUser = User.builder()
                .id(1L)
                .email("admin@apollo-hospital.org")
                .fullName("Apollo Admin")
                .role(Role.HOSPITAL)
                .active(true)
                .build();

        hospital = Hospital.builder()
                .id(10L)
                .user(hospitalUser)
                .hospitalName("Apollo Emergency Care")
                .email("admin@apollo-hospital.org")
                .address("100 Bannerghatta Road")
                .city("Bangalore")
                .state("Karnataka")
                .build();

        donorUser = User.builder()
                .id(2L)
                .email("rahul.donor@example.com")
                .fullName("Rahul Verma")
                .role(Role.DONOR)
                .active(true)
                .build();

        donor = DonorProfile.builder()
                .id(20L)
                .user(donorUser)
                .email("rahul.donor@example.com")
                .bloodGroup(BloodGroup.O_POSITIVE)
                .city("Bangalore")
                .state("Karnataka")
                .build();

        bloodRequest = BloodRequest.builder()
                .id(400L)
                .hospital(hospital)
                .bloodGroupNeeded(BloodGroup.O_POSITIVE)
                .unitsRequired(2)
                .urgencyLevel(UrgencyLevel.CRITICAL)
                .requiredByDate(LocalDate.now().plusDays(1))
                .reason("Critical Surgery")
                .build();
    }

    @Test
    @DisplayName("Should create hospital dashboard notification and dispatch acceptance email to hospital")
    void testCreateDonorAcceptedNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createDonorAcceptedNotification(donor, hospital, bloodRequest);

        // 1. In-app dashboard notification is saved
        verify(notificationRepository, times(1)).save(any(Notification.class));

        // 2. Acceptance email sent to hospital's registered email
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> hospNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> donorNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bgCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> reqIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> unitsCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(emailService, times(1)).sendDonorAcceptanceEmailToHospital(
                emailCaptor.capture(),
                hospNameCaptor.capture(),
                donorNameCaptor.capture(),
                bgCaptor.capture(),
                reqIdCaptor.capture(),
                unitsCaptor.capture(),
                any(Double.class),
                any(String.class)
        );

        assertEquals("admin@apollo-hospital.org", emailCaptor.getValue());
        assertEquals("Apollo Emergency Care", hospNameCaptor.getValue());
        assertEquals("Rahul Verma", donorNameCaptor.getValue());
        assertEquals("O+", bgCaptor.getValue());
        assertEquals(400L, reqIdCaptor.getValue());
        assertEquals(2, unitsCaptor.getValue());
    }

    @Test
    @DisplayName("Should handle email dispatch exception gracefully without affecting dashboard notification")
    void testCreateDonorAcceptedNotification_EmailFailureHandledGracefully() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("SMTP Connection Timed Out"))
                .when(emailService).sendDonorAcceptanceEmailToHospital(
                        any(), any(), any(), any(), any(), any(), any(), any()
                );

        assertDoesNotThrow(() -> {
            notificationService.createDonorAcceptedNotification(donor, hospital, bloodRequest);
        });

        // Dashboard notification must still be persisted
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}
