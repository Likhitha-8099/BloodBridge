package com.bloodbridge.dto.response;

import com.bloodbridge.enums.BloodGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing blood bank inventory item response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Blood Bank Inventory Response Payload")
public class BloodInventoryResponse {

    @Schema(description = "Inventory entry ID", example = "1")
    private Long id;

    @Schema(description = "Blood group", example = "O_POSITIVE")
    private BloodGroup bloodGroup;

    @Schema(description = "Available blood units in stock", example = "15")
    private Integer availableUnits;

    @Schema(description = "Reserved units for active requests", example = "2")
    private Integer reservedUnits;

    @Schema(description = "Critical threshold count", example = "5")
    private Integer criticalThreshold;

    @Schema(description = "Stock status (NORMAL, LOW, CRITICAL, OUT_OF_STOCK)", example = "NORMAL")
    private String inventoryStatus;

    @Schema(description = "Last updated timestamp", example = "2026-08-01T11:00:00")
    private LocalDateTime updatedAt;
}
