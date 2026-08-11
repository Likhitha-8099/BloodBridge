package com.bloodbridge.controller;

import com.bloodbridge.dto.DonorMatchingResult;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.EmergencyNotificationStatsDTO;
import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.enums.EmailDeliveryStatus;
import com.bloodbridge.repository.BloodRequestRepository;
import com.bloodbridge.repository.EmailNotificationRepository;
import com.bloodbridge.service.DonorMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller providing Admin Dashboard analytics for Smart Emergency Donor Notification runs.
 */
@RestController
@RequestMapping({"/api/v1/admin/emergency-stats", "/api/admin/emergency-stats"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Emergency Notification Analytics", description = "Endpoints for inspecting smart emergency notification metrics")
public class AdminEmergencyStatsController {

    private final BloodRequestRepository bloodRequestRepository;
    private final EmailNotificationRepository emailNotificationRepository;
    private final DonorMatchingService donorMatchingService;

    @Operation(summary = "Get Emergency Request Dispatch Telemetry", description = "Returns detailed notification statistics for an emergency blood request.")
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<EmergencyNotificationStatsDTO>> getEmergencyNotificationStats(@PathVariable("requestId") Long requestId) {
        log.info("[ADMIN-STATS] Fetching notification telemetry for Emergency Request #{}", requestId);

        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElse(null);

        long totalEvaluated = 0;
        long compatible = 0;
        long within50Km = 0;

        if (request != null) {
            DonorMatchingResult result = donorMatchingService.evaluateEligibleDonors(request);
            totalEvaluated = result.getTotalEvaluatedCount();
            compatible = result.getCompatibleCount();
            within50Km = result.getWithinRadiusCount();
        }

        long emailsQueued = emailNotificationRepository.countByEmergencyRequestIdAndStatus(requestId, EmailDeliveryStatus.PENDING);
        long emailsSent = emailNotificationRepository.countByEmergencyRequestIdAndStatus(requestId, EmailDeliveryStatus.SENT);
        long emailsFailed = emailNotificationRepository.countByEmergencyRequestIdAndStatus(requestId, EmailDeliveryStatus.FAILED);
        long emailsSkipped = emailNotificationRepository.countByEmergencyRequestIdAndStatus(requestId, EmailDeliveryStatus.SKIPPED);

        Double avgSmtpTime = emailNotificationRepository.findAverageSmtpTimeMsByEmergencyRequestId(requestId);

        EmergencyNotificationStatsDTO stats = EmergencyNotificationStatsDTO.builder()
                .emergencyRequestId(requestId)
                .totalDonorsEvaluated(totalEvaluated)
                .compatibleDonors(compatible)
                .within50KmDonors(within50Km)
                .emailsQueued(emailsQueued)
                .emailsSent(emailsSent)
                .emailsFailed(emailsFailed)
                .emailsSkipped(emailsSkipped)
                .averageSmtpTimeMs(avgSmtpTime != null ? Math.round(avgSmtpTime * 100.0) / 100.0 : 0.0)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Emergency notification stats retrieved successfully", stats));
    }
}
