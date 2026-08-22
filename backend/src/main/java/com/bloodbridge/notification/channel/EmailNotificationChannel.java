package com.bloodbridge.notification.channel;

import com.bloodbridge.dto.EmergencyMailDto;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.notification.NotificationChannel;
import com.bloodbridge.notification.NotificationPayload;
import com.bloodbridge.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy Channel for Email Notification Dispatch.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationChannel implements NotificationChannel {

    private final EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public DeliveryChannel getChannel() {
        return DeliveryChannel.EMAIL;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean send(NotificationPayload payload) {
        if (!isEnabled()) return false;

        String recipientEmail = payload.getRecipientEmail();
        if ((recipientEmail == null || recipientEmail.isBlank()) && payload.getRecipientUser() != null) {
            recipientEmail = payload.getRecipientUser().getEmail();
        }
        if ((recipientEmail == null || recipientEmail.isBlank()) && payload.getRecipientDonor() != null) {
            recipientEmail = payload.getRecipientDonor().getEmail();
            if ((recipientEmail == null || recipientEmail.isBlank()) && payload.getRecipientDonor().getUser() != null) {
                recipientEmail = payload.getRecipientDonor().getUser().getEmail();
            }
        }

        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("[EMAIL-FAILURE] Email type: EMERGENCY_ALERT, Recipient: NULL/BLANK, Reason: Missing recipient email address");
            return false;
        }

        Long donorId = payload.getRecipientDonor() != null ? payload.getRecipientDonor().getId() : null;
        Long requestId = payload.getEmergencyRequestId();

        log.info("[EMAIL-EMERGENCY-QUEUE] Request ID: #{}, Donor ID: #{}, Email: {}", requestId != null ? requestId : "N/A", donorId != null ? donorId : "N/A", recipientEmail);

        try {
            if (payload.getBloodRequest() != null) {
                var req = payload.getBloodRequest();
                var hospital = (req != null && req.getHospital() != null) ? req.getHospital() : payload.getHospital();
                String donorName = "Valued Donor";
                if (payload.getRecipientUser() != null && payload.getRecipientUser().getFullName() != null && !payload.getRecipientUser().getFullName().isBlank()) {
                    donorName = payload.getRecipientUser().getFullName();
                } else if (payload.getRecipientDonor() != null && payload.getRecipientDonor().getUser() != null && payload.getRecipientDonor().getUser().getFullName() != null && !payload.getRecipientDonor().getUser().getFullName().isBlank()) {
                    donorName = payload.getRecipientDonor().getUser().getFullName();
                }

                EmergencyMailDto mailDto = EmergencyMailDto.builder()
                        .requestId(requestId)
                        .donorId(donorId)
                        .distanceKm(payload.getExtraData() != null && payload.getExtraData().get("distanceKm") != null ? ((Number) payload.getExtraData().get("distanceKm")).doubleValue() : 0.0)
                        .toEmail(recipientEmail)
                        .donorName(donorName)
                        .hospitalName(hospital != null && hospital.getHospitalName() != null ? hospital.getHospitalName() : "Emergency Hospital")
                        .bloodGroup(req.getBloodGroupNeeded() != null ? req.getBloodGroupNeeded().name() : "ANY")
                        .unitsRequired(req.getUnitsRequired() != null ? req.getUnitsRequired() : 1)
                        .urgencyLevel(req.getUrgencyLevel() != null ? req.getUrgencyLevel().name() : "HIGH")
                        .hospitalAddress(hospital != null && hospital.getAddress() != null ? hospital.getAddress() : "")
                        .city(hospital != null && hospital.getCity() != null ? hospital.getCity() : "")
                        .state(hospital != null && hospital.getState() != null ? hospital.getState() : "")
                        .requiredByDate(req.getRequiredByDate() != null ? req.getRequiredByDate().toString() : "")
                        .reason(req.getReason() != null ? req.getReason() : "Urgent Blood Need")
                        .loginUrl(frontendUrl + "/donor/requests")
                        .build();

                emailService.sendEmergencyAlert(mailDto);
            } else {
                emailService.sendEmail(recipientEmail, payload.getTitle(), payload.getMessage());
            }
            log.info("[EMAIL-DONOR] SUCCESS");
            return true;
        } catch (Exception e) {
            log.error("[EMAIL-DONOR] FAILED\nrequestId={}\ndonorId={}\nemail={}\nreason={}",
                    requestId != null ? requestId : "N/A",
                    donorId != null ? donorId : "N/A",
                    recipientEmail,
                    e.getMessage());
            return false;
        }
    }
}
