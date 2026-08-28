package com.bloodbridge.controller;

import com.bloodbridge.service.impl.HttpApiEmailTransportServiceImpl;
import com.bloodbridge.service.impl.SmtpEmailTransportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailDebugControllerTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private HttpApiEmailTransportServiceImpl httpApiTransport;

    @Mock
    private SmtpEmailTransportServiceImpl smtpTransport;

    private EmailDebugController controller;

    @BeforeEach
    void setUp() {
        controller = new EmailDebugController(mailSender, httpApiTransport, smtpTransport);
    }

    @Test
    void getSmtpStatus_WithResendProvider_ReportsResendActiveTransport() {
        ReflectionTestUtils.setField(controller, "emailProvider", "resend");
        ReflectionTestUtils.setField(controller, "resendApiKey", "re_test_key_12345");
        ReflectionTestUtils.setField(controller, "resendFrom", "BloodBridge <onboarding@resend.dev>");
        when(httpApiTransport.getProviderName()).thenReturn("RESEND_HTTPS_API");
        when(httpApiTransport.isConfigured()).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.getSmtpStatus();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("emailProvider")).isEqualTo("resend");
        assertThat(body.get("activeTransport")).isEqualTo("RESEND_HTTPS_API");
        assertThat(body.get("isTransportConfigured")).isEqualTo(true);
        assertThat(body.get("isResendConfigured")).isEqualTo(true);
        assertThat(body.get("resendFrom")).isEqualTo("BloodBridge <onboarding@resend.dev>");
    }

    @Test
    void sendTestEmail_WithResendProvider_DispatchesViaHttpTransport() {
        ReflectionTestUtils.setField(controller, "emailProvider", "resend");
        ReflectionTestUtils.setField(controller, "resendApiKey", "re_test_key_12345");
        when(httpApiTransport.getProviderName()).thenReturn("RESEND_HTTPS_API");
        when(httpApiTransport.isConfigured()).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.sendTestEmail("test@example.com", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo("SUCCESS");
        assertThat(body.get("activeTransport")).isEqualTo("RESEND_HTTPS_API");

        verify(httpApiTransport, times(1)).sendHtmlEmail(
                eq("test@example.com"),
                eq("🚨 [TEST] BloodBridge Emergency Alert Pipeline Verification"),
                anyString(),
                eq("BloodBridge Team"),
                isNull(),
                isNull()
        );
        verifyNoInteractions(smtpTransport);
    }

    @Test
    void sendTestEmail_WhenTransportNotConfigured_ReturnsBadRequest() {
        ReflectionTestUtils.setField(controller, "emailProvider", "resend");
        ReflectionTestUtils.setField(controller, "resendApiKey", "");
        when(httpApiTransport.getProviderName()).thenReturn("RESEND_HTTPS_API");
        when(httpApiTransport.isConfigured()).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.sendTestEmail("test@example.com", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo("FAILED_CONFIGURATION");
        assertThat(body.get("error").toString()).contains("RESEND_API_KEY");

        verify(httpApiTransport, never()).sendHtmlEmail(anyString(), anyString(), anyString(), anyString(), any(), any());
    }
}
