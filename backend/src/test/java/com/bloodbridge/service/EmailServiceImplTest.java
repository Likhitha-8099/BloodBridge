package com.bloodbridge.service;

import com.bloodbridge.dto.EmergencyMailDto;
import com.bloodbridge.service.impl.EmailServiceImpl;
import com.bloodbridge.service.impl.HttpApiEmailTransportServiceImpl;
import com.bloodbridge.service.impl.SmtpEmailTransportServiceImpl;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EmailServiceImpl} validating dual transport operation.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private com.bloodbridge.repository.EmailNotificationRepository emailNotificationRepository;

    private SmtpEmailTransportServiceImpl smtpTransport;
    private HttpApiEmailTransportServiceImpl httpApiTransport;
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        smtpTransport = new SmtpEmailTransportServiceImpl(mailSender);
        httpApiTransport = new HttpApiEmailTransportServiceImpl();
        emailService = new EmailServiceImpl(smtpTransport, httpApiTransport, emailNotificationRepository);
    }

    @Test
    void sendEmail_Success() {
        emailService.sendEmail("test@example.com", "Test Subject", "Test Body");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmergencyAlert_Success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        EmergencyMailDto mailDto = EmergencyMailDto.builder()
                .toEmail("donor@example.com")
                .donorName("John Donor")
                .hospitalName("General Hospital")
                .bloodGroup("O_POSITIVE")
                .unitsRequired(2)
                .urgencyLevel("HIGH")
                .hospitalAddress("123 Health Ave")
                .city("Bangalore")
                .state("Karnataka")
                .requiredByDate("2026-08-10")
                .reason("Emergency Surgery")
                .loginUrl("http://localhost:5173/login")
                .build();

        emailService.sendEmergencyAlert(mailDto);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendEmergencyAlert_InvalidRecipient_Skipped() {
        EmergencyMailDto mailDto = EmergencyMailDto.builder()
                .toEmail("")
                .donorName("John Donor")
                .build();

        emailService.sendEmergencyAlert(mailDto);

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEmergencyAlert_SmtpException_HandledGracefully() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP Authentication Failed 535"))
                .when(mailSender).send(any(MimeMessage.class));

        EmergencyMailDto mailDto = EmergencyMailDto.builder()
                .toEmail("donor@example.com")
                .donorName("John Donor")
                .hospitalName("General Hospital")
                .bloodGroup("O_POSITIVE")
                .unitsRequired(2)
                .urgencyLevel("HIGH")
                .build();

        // Should not throw exception upstream
        emailService.sendEmergencyAlert(mailDto);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendDonationConfirmationEmail_Success() {
        emailService.sendDonationConfirmationEmail("donor@example.com", "Sarah", "John", "City Hospital");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendBloodRequestEmail_Success() {
        emailService.sendBloodRequestEmail("hospital@example.com", "A_POSITIVE", 3);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendMatchNotificationEmail_Success() {
        emailService.sendMatchNotificationEmail("donor@example.com", "B_NEGATIVE", "City Clinic");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendHospitalApproval_Success() {
        emailService.sendHospitalApproval("hospital.admin@example.com", "Apollo Hospital");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendDonationCertificateEmail_Success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        byte[] pdfBytes = "Sample Certificate PDF Bytes".getBytes();

        emailService.sendDonationCertificateEmail(
                "donor@example.com",
                "Rahul Sharma",
                "Apollo Hospital",
                "O_POSITIVE",
                1,
                "2026-08-24",
                "CERT-12345",
                pdfBytes
        );

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendDonorAcceptanceEmailToHospital_Success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendDonorAcceptanceEmailToHospital(
                "admin@apollo.org",
                "Apollo Hospital",
                "Rahul Sharma",
                "O_POSITIVE",
                501L,
                2,
                3.5,
                "2026-08-24 16:30:00"
        );

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendEmergencyAlert_WithResendProvider_UsesHttpApiTransportAndBypassesSmtp() {
        HttpApiEmailTransportServiceImpl mockHttpTransport = mock(HttpApiEmailTransportServiceImpl.class);
        when(mockHttpTransport.getProviderName()).thenReturn("RESEND_HTTPS_API");
        
        EmailServiceImpl resendEmailService = new EmailServiceImpl(smtpTransport, mockHttpTransport, emailNotificationRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(resendEmailService, "emailProvider", "resend");

        EmergencyMailDto mailDto = EmergencyMailDto.builder()
                .requestId(999L)
                .donorId(888L)
                .toEmail("donor@example.com")
                .donorName("Resend Donor")
                .hospitalName("City General")
                .bloodGroup("A_POSITIVE")
                .unitsRequired(1)
                .urgencyLevel("HIGH")
                .build();

        resendEmailService.sendEmergencyAlert(mailDto);

        // Verify HTTP API transport was invoked with HTML email
        verify(mockHttpTransport, times(1)).sendHtmlEmail(eq("donor@example.com"), anyString(), anyString(), anyString(), isNull(), isNull());

        // Verify JavaMailSender / SMTP was NEVER called
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendDonationCertificateEmail_WithResendProvider_PassesPdfBytesToHttpTransport() {
        HttpApiEmailTransportServiceImpl mockHttpTransport = mock(HttpApiEmailTransportServiceImpl.class);
        when(mockHttpTransport.getProviderName()).thenReturn("RESEND_HTTPS_API");

        EmailServiceImpl resendEmailService = new EmailServiceImpl(smtpTransport, mockHttpTransport, emailNotificationRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(resendEmailService, "emailProvider", "resend");

        byte[] pdfBytes = "Sample Certificate".getBytes();
        resendEmailService.sendDonationCertificateEmail(
                "donor@example.com",
                "Resend Donor",
                "City General",
                "A_POSITIVE",
                1,
                "2026-08-28",
                "CERT-RESEND-001",
                pdfBytes
        );

        // Verify HTTP API transport was called with attachment
        verify(mockHttpTransport, times(1)).sendHtmlEmail(
                eq("donor@example.com"),
                eq("BloodBridge Donation Certificate"),
                anyString(),
                eq("BloodBridge Team"),
                eq(pdfBytes),
                eq("BloodBridge_Certificate_CERT-RESEND-001.pdf")
        );

        // Verify SMTP was NEVER called
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
