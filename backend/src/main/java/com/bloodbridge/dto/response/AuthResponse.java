package com.bloodbridge.dto.response;

import com.bloodbridge.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing the authentication response containing JWT access token and user info.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authentication Response Payload")
public class AuthResponse {

    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Authenticated user email", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Assigned user role", example = "DONOR")
    private Role role;

    @Schema(description = "Detailed user information")
    private UserInfo user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "User summary information")
    public static class UserInfo {

        @Schema(description = "User unique ID", example = "1")
        private Long id;

        @Schema(description = "Full name", example = "John Doe")
        private String fullName;

        @Schema(description = "Email address", example = "john.doe@example.com")
        private String email;

        @Schema(description = "Contact phone number", example = "+1234567890")
        private String phoneNumber;

        @Schema(description = "Primary user role", example = "DONOR")
        private Role role;

        @Schema(description = "Account active status", example = "true")
        private Boolean active;

        private String city;
        private String state;
    }
}
