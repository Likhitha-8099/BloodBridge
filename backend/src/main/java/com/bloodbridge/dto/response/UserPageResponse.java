package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object for paginated User lists.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Paginated User List Response Payload")
public class UserPageResponse {

    @Schema(description = "List of user profiles on current page")
    private List<UserProfileResponse> content;

    @Schema(description = "Current zero-based page number", example = "0")
    private int pageNumber;

    @Schema(description = "Requested page size", example = "10")
    private int pageSize;

    @Schema(description = "Total matching items count", example = "42")
    private long totalElements;

    @Schema(description = "Total calculated pages count", example = "5")
    private int totalPages;

    @Schema(description = "Flag indicating if this is the final page", example = "false")
    private boolean last;
}
