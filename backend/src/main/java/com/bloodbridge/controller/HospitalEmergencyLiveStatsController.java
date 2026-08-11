package com.bloodbridge.controller;

import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.HospitalEmergencyLiveStatsDTO;
import com.bloodbridge.service.EmergencyResponseService;
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
 * REST controller serving live hospital dashboard telemetry and accepted donor details.
 */
@RestController
@RequestMapping({"/api/v1/hospital/emergency-requests", "/api/hospital/emergency-requests"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Hospital Emergency Live Stats", description = "Endpoints for hospitals to fetch real-time response telemetry")
public class HospitalEmergencyLiveStatsController {

    private final EmergencyResponseService emergencyResponseService;

    @Operation(summary = "Get Live Hospital Emergency Telemetry", description = "Returns live response counts, accepted donor details, ETA, and response times for a request.")
    @GetMapping("/{requestId}/live-stats")
    public ResponseEntity<ApiResponse<HospitalEmergencyLiveStatsDTO>> getLiveStats(@PathVariable("requestId") Long requestId) {
        log.info("[HOSPITAL-LIVE-STATS] Fetching live stats for Emergency Request #{}", requestId);
        HospitalEmergencyLiveStatsDTO stats = emergencyResponseService.getHospitalLiveStats(requestId);
        return ResponseEntity.ok(ApiResponse.success("Hospital emergency live stats retrieved successfully", stats));
    }
}
