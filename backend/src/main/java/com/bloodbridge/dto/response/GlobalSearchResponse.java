package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object representing Global Admin Search results across entities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Global Admin Search Response Payload")
public class GlobalSearchResponse {

    @Schema(description = "Matching users count", example = "5")
    private long usersCount;

    @Schema(description = "Matching hospitals count", example = "2")
    private long hospitalsCount;

    @Schema(description = "Matching requests count", example = "4")
    private long requestsCount;

    @Schema(description = "Matching donations count", example = "3")
    private long donationsCount;

    @Schema(description = "List of matching summary items")
    private List<SearchResultItem> results;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Single Search Result Item")
    public static class SearchResultItem {
        @Schema(description = "Entity type (USER, HOSPITAL, BLOOD_REQUEST, DONATION)", example = "HOSPITAL")
        private String entityType;

        @Schema(description = "Entity ID", example = "1")
        private Long entityId;

        @Schema(description = "Entity title or name", example = "Boston General Hospital")
        private String title;

        @Schema(description = "Search item subtitle or description", example = "Boston, MA - Registration: MA-HOSP-99821")
        private String subtitle;

        @Schema(description = "Item status", example = "APPROVED")
        private String status;
    }
}
