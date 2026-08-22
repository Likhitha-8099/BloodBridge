package com.bloodbridge.service;

import com.bloodbridge.dto.EmergencyMailDto;
import com.bloodbridge.entity.*;
import com.bloodbridge.enums.*;
import com.bloodbridge.notification.NotificationPayload;
import com.bloodbridge.notification.channel.EmailNotificationChannel;
import com.bloodbridge.repository.EmailNotificationRepository;
import com.bloodbridge.service.impl.EmailServiceImpl;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Enterprise test suite verifying the complete Emergency Blood Request Email notification flow,
 * template rendering, database persistence of EmailNotification status, resilience, and duplicate prevention.
 */
@ExtendWith(MockitoExtension.class)
public class EmergencyEmailDispatchFlowTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailNotificationRepository emailNotificationRepository;

    private EmailServiceImpl emailService;

    private EmailNotificationChannel emailNotificationChannel;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, emailNotificationRepository);
        emailService.init();
        emailNotificationChannel = new EmailNotificationChannel(emailService);
    }

    @Test
    @DisplayName("Should successfully send emergency alert HTML email and persist SENT status in repository")
    void testSendEmergencyAlert_Success_And_RecordStatus() {
        MimeMessage realMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        when(emailNotificationRepository.findByEmergencyRequestIdAndDonorId(101L, 202L))
                .thenReturn(Optional.empty());

        EmergencyMailDto mailDto = EmergencyMailDto.builder()
                .requestId(101L)
                .donorId(202L)
                .toEmail("eligible.donor@example.com")
                .donorName("Ananya Sharma")
                .hospitalName("Apollo Emergency Hospital")
                .bloodGroup("O_POSITIVE")
                .unitsRequired(3)
                .urgencyLevel("CRITICAL")
                .hospitalAddress("Bannerghatta Main Rd, Bangalore")
                .city("Bangalore")
                .state("Karnataka")
                .requiredByDate("2026-08-20")
                .reason("Critical Cardiac Surgery")
                .loginUrl("http://localhost:5173/donor/requests")
                .build();

        emailService.sendEmergencyAlert(mailDto);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));

        ArgumentCaptor<EmailNotification> notifCaptor = ArgumentCaptor.forClass(EmailNotification.class);
        verify(emailNotificationRepository, times(1)).save(notifCaptor.capture());

        EmailNotification savedNotif = notifCaptor.getValue();
        assertThat(savedNotif.getEmergencyRequestId()).isEqualTo(101L);
        assertThat(savedNotif.getDonorId()).isEqualTo(202L);
        assertThat(savedNotif.getEmail()).isEqualTo("eligible.donor@example.com");
        assertThat(savedNotif.getStatus()).isEqualTo(EmailDeliveryStatus.SENT);
        assertThat(savedNotif.getSentAt()).isNotNull();
        assertThat(savedNotif.getDeliveryAttempts()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle SMTP delivery failure gracefully without throwing and record FAILED status")
    void testSendEmergencyAlert_SmtpFailure_RecordsFailedStatus() {
        MimeMessage realMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        doThrow(new RuntimeException("535 5.7.8 Username and Password not accepted"))
                .when(mailSender).send(any(MimeMessage.class));
        when(emailNotificationRepository.findByEmergencyRequestIdAndDonorId(102L, 203L))
                .thenReturn(Optional.empty());

        EmergencyMailDto mailDto = EmergencyMailDto.builder()
                .requestId(102L)
                .donorId(203L)
                .toEmail("rahul.donor@example.com")
                .donorName("Rahul Verma")
                .hospitalName("Fortis Hospital")
                .bloodGroup("A_POSITIVE")
                .unitsRequired(2)
                .urgencyLevel("HIGH")
                .build();

        // Must not throw upstream exception
        emailService.sendEmergencyAlert(mailDto);

        ArgumentCaptor<EmailNotification> notifCaptor = ArgumentCaptor.forClass(EmailNotification.class);
        verify(emailNotificationRepository, times(1)).save(notifCaptor.capture());

        EmailNotification savedNotif = notifCaptor.getValue();
        assertThat(savedNotif.getStatus()).isEqualTo(EmailDeliveryStatus.FAILED);
        assertThat(savedNotif.getFailureReason()).contains("Username and Password not accepted");
    }

    @Test
    @DisplayName("Should prevent duplicate emergency email dispatch for same request and donor")
    void testSendEmergencyAlert_DuplicatePrevention() {
        MimeMessage realMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);

        EmergencyMailDto mailDto = EmergencyMailDto.builder()
                .requestId(999L)
                .donorId(888L)
                .toEmail("unique.donor@example.com")
                .donorName("Priya")
                .hospitalName("Manipal Hospital")
                .bloodGroup("B_POSITIVE")
                .unitsRequired(1)
                .urgencyLevel("HIGH")
                .build();

        // First dispatch: should send
        emailService.sendEmergencyAlert(mailDto);
        verify(mailSender, times(1)).send(any(MimeMessage.class));

        // Second dispatch: should be skipped by idempotency filter
        emailService.sendEmergencyAlert(mailDto);
        verify(mailSender, times(1)).send(any(MimeMessage.class)); // still 1
    }

    @Test
    @DisplayName("EmailNotificationChannel should parse payload and invoke EmailService correctly")
    void testEmailNotificationChannel_DispatchesEmergencyMail() {
        MimeMessage realMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);

        User hospitalUser = User.builder().id(10L).fullName("Admin").email("hospital@city.org").build();
        Hospital hospital = Hospital.builder()
                .id(1L)
                .user(hospitalUser)
                .hospitalName("City Care Hospital")
                .address("MG Road 45")
                .city("Hyderabad")
                .state("Telangana")
                .build();

        BloodRequest bloodRequest = BloodRequest.builder()
                .id(55L)
                .hospital(hospital)
                .bloodGroupNeeded(BloodGroup.AB_POSITIVE)
                .unitsRequired(4)
                .urgencyLevel(UrgencyLevel.CRITICAL)
                .reason("Accident Trauma Case")
                .requiredByDate(LocalDate.now().plusDays(1))
                .requestDate(LocalDateTime.now())
                .status(RequestStatus.CREATED)
                .build();

        User donorUser = User.builder().id(20L).fullName("Vikram Reddy").email("vikram.reddy@test.com").build();
        DonorProfile donor = DonorProfile.builder()
                .id(15L)
                .user(donorUser)
                .email("vikram.reddy@test.com")
                .bloodGroup(BloodGroup.AB_POSITIVE)
                .city("Hyderabad")
                .state("Telangana")
                .build();

        NotificationPayload payload = NotificationPayload.builder()
                .emergencyRequestId(55L)
                .recipientUser(donorUser)
                .recipientDonor(donor)
                .hospital(hospital)
                .bloodRequest(bloodRequest)
                .recipientEmail("vikram.reddy@test.com")
                .title("🚨 Emergency AB+ blood needed at City Care Hospital")
                .message("Immediate donation required for trauma emergency.")
                .notificationType(NotificationType.EMERGENCY_BLOOD_REQUEST)
                .priority("HIGH")
                .extraData(Map.of("distanceKm", 4.2))
                .build();

        boolean result = emailNotificationChannel.send(payload);

        assertThat(result).isTrue();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
