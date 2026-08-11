package com.bloodbridge.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object representing a user login request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password")
@io.swagger.v3.oas.annotations.media.Schema(description = "User Login Request Payload")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @io.swagger.v3.oas.annotations.media.Schema(description = "Registered email address", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @io.swagger.v3.oas.annotations.media.Schema(description = "Account password", example = "BloodBridge@2026")
    private String password;
}
