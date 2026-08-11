package com.bloodbridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for setting or uploading a Profile Image URL.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Profile Image URL Upload Request Payload")
public class ProfileImageUploadRequest {

    @NotBlank(message = "Profile image URL is required")
    @Schema(description = "Direct URL or storage key for profile picture", example = "https://images.bloodbridge.com/profiles/avatar_123.jpg")
    private String imageUrl;
}
