package com.bloodbridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object for Password Change operations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"currentPassword", "newPassword", "confirmPassword"})
@Schema(description = "User Change Password Request Payload")
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    @Schema(description = "Existing account password", example = "BloodBridge@2026")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
        message = "New password must be at least 8 characters and contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@#$%^&+=!)"
    )
    @Schema(description = "New account password matching security rules", example = "BloodBridge@2027")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "Confirmation matching new password", example = "BloodBridge@2027")
    private String confirmPassword;
}
