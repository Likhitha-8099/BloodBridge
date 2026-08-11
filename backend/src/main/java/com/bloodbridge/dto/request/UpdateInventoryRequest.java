package com.bloodbridge.dto.request;

import com.bloodbridge.enums.BloodGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for updating blood bank stock inventory.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Update Blood Bank Inventory Request Payload")
public class UpdateInventoryRequest {

    @NotNull(message = "Blood group is required")
    @Schema(description = "Blood group enum", example = "O_POSITIVE")
    private BloodGroup bloodGroup;

    @NotNull(message = "Available units count is required")
    @Min(value = 0, message = "Available units cannot be negative")
    @Schema(description = "Available units count", example = "15")
    private Integer availableUnits;

    @Min(value = 0, message = "Reserved units cannot be negative")
    @Schema(description = "Reserved units count", example = "2")
    private Integer reservedUnits;

    @Min(value = 1, message = "Critical threshold must be at least 1")
    @Schema(description = "Critical threshold before triggering LOW/CRITICAL alert", example = "5")
    private Integer criticalThreshold;
}
