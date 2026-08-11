package com.bloodbridge.controller;

import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.entity.EmergencyTimelineEvent;
import com.bloodbridge.service.EmergencyTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller providing timeline tracking endpoints for emergency requests.
 */
@RestController
@RequestMapping({"/api/v1/admin/emergency", "/api/admin/emergency"})
@RequiredArgsConstructor
public class EmergencyTimelineController {

    private final EmergencyTimelineService timelineService;

    @GetMapping("/{requestId}/timeline")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL')")
    public ResponseEntity<ApiResponse<List<EmergencyTimelineEvent>>> getEmergencyTimeline(@PathVariable Long requestId) {
        List<EmergencyTimelineEvent> timeline = timelineService.getTimelineForRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success("Emergency timeline fetched successfully", timeline));
    }
}
