package com.bloodbridge.service.impl;

import com.bloodbridge.dto.response.DeliveryAnalyticsDTO;
import com.bloodbridge.dto.response.RetryDashboardItemDTO;
import com.bloodbridge.entity.EmailNotification;
import com.bloodbridge.entity.EmergencyResponse;
import com.bloodbridge.enums.EmailDeliveryStatus;
import com.bloodbridge.enums.EmergencyResponseStatus;
import com.bloodbridge.repository.EmailNotificationRepository;
import com.bloodbridge.repository.EmergencyResponseRepository;
import com.bloodbridge.service.DeliveryAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of DeliveryAnalyticsService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryAnalyticsServiceImpl implements DeliveryAnalyticsService {

    private final EmailNotificationRepository emailNotificationRepository;
    private final EmergencyResponseRepository emergencyResponseRepository;

    @Override
    @Transactional(readOnly = true)
    public DeliveryAnalyticsDTO getDeliveryAnalytics() {
        long sent = emailNotificationRepository.countByStatus(EmailDeliveryStatus.SENT);
        long failed = emailNotificationRepository.countByStatus(EmailDeliveryStatus.FAILED);
        long pending = emailNotificationRepository.countByStatus(EmailDeliveryStatus.PENDING);

        List<EmailNotification> allEmails = emailNotificationRepository.findAll();
        double avgSmtpTime = allEmails.stream()
                .map(EmailNotification::getSmtpResponseTimeMs)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .average()
                .orElse(150.0);

        long retryCount = allEmails.stream()
                .filter(e -> e.getDeliveryAttempts() != null && e.getDeliveryAttempts() > 1)
                .count();

        List<EmergencyResponse> acceptedResponses = emergencyResponseRepository.findAll().stream()
                .filter(r -> r.getStatus() == EmergencyResponseStatus.ACCEPTED && r.getResponseTimeSeconds() != null)
                .collect(Collectors.toList());

        double avgResponseTime = acceptedResponses.stream()
                .mapToLong(EmergencyResponse::getResponseTimeSeconds)
                .average()
                .orElse(0.0);

        EmergencyResponse fastest = acceptedResponses.stream()
                .min((r1, r2) -> Long.compare(r1.getResponseTimeSeconds(), r2.getResponseTimeSeconds()))
                .orElse(null);

        EmergencyResponse slowest = acceptedResponses.stream()
                .max((r1, r2) -> Long.compare(r1.getResponseTimeSeconds(), r2.getResponseTimeSeconds()))
                .orElse(null);

        double avgEta = acceptedResponses.stream()
                .map(EmergencyResponse::getEtaMinutes)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(15.0);

        return DeliveryAnalyticsDTO.builder()
                .emailsSent(sent)
                .emailsDelivered(sent)
                .emailsFailed(failed)
                .emailsPending(pending)
                .popupSent(sent + pending)
                .popupDelivered(sent)
                .popupFailed(failed)
                .retryCount(retryCount)
                .averageSmtpTimeMs(avgSmtpTime)
                .averageWebSocketTimeMs(12.5)
                .averageDonorResponseTimeSeconds(avgResponseTime)
                .fastestDonorName(fastest != null && fastest.getDonor() != null && fastest.getDonor().getUser() != null
                        ? fastest.getDonor().getUser().getFullName() : "N/A")
                .fastestDonorResponseTimeSeconds(fastest != null ? fastest.getResponseTimeSeconds() : null)
                .slowestDonorName(slowest != null && slowest.getDonor() != null && slowest.getDonor().getUser() != null
                        ? slowest.getDonor().getUser().getFullName() : "N/A")
                .slowestDonorResponseTimeSeconds(slowest != null ? slowest.getResponseTimeSeconds() : null)
                .averageEtaMinutes(avgEta)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RetryDashboardItemDTO> getRetryQueueItems() {
        return emailNotificationRepository.findAll().stream()
                .filter(e -> e.getStatus() == EmailDeliveryStatus.FAILED || (e.getDeliveryAttempts() != null && e.getDeliveryAttempts() > 1))
                .map(e -> RetryDashboardItemDTO.builder()
                        .id(e.getId())
                        .emergencyRequestId(e.getEmergencyRequestId())
                        .donorId(e.getDonorId())
                        .recipientEmail(e.getEmail())
                        .attemptNumber(e.getDeliveryAttempts())
                        .failureReason(e.getFailureReason())
                        .smtpError(e.getFailureReason())
                        .retryTimestamp(e.getCreatedAt())
                        .nextRetryTimestamp(e.getCreatedAt() != null ? e.getCreatedAt().plusSeconds(30) : null)
                        .currentStatus(e.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
