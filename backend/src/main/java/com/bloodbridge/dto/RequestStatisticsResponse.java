package com.bloodbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing blood request statistics for the admin dashboard.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestStatisticsResponse {
    private Long totalRequests;
    private Long pendingRequests;
    private Long verifiedRequests;
    private Long matchedRequests;
    private Long completedRequests;
    private Long cancelledRequests;
    private Long rejectedRequests;
}
