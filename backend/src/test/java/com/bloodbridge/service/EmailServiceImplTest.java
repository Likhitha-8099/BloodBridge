package com.bloodbridge.service;

import com.bloodbridge.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EmailServiceImpl}.
 */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendEmail_Success() {
        emailService.sendEmail("test@example.com", "Test Subject", "Test Body");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
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
}
