package com.bloodbridge.mapper;

import com.bloodbridge.dto.request.RegisterRequest;
import com.bloodbridge.dto.request.UpdateProfileRequest;
import com.bloodbridge.dto.response.AuthResponse;
import com.bloodbridge.dto.response.UserProfileResponse;
import com.bloodbridge.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper utility class to convert between User entities and DTOs.
 */
@Component
public class UserMapper {

    /**
     * Converts a {@link RegisterRequest} DTO to a {@link User} entity.
     *
     * @param request registration request DTO
     * @return User entity
     */
    public User toEntity(RegisterRequest request) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .roles(request.getRole() != null ? new java.util.HashSet<>(java.util.List.of(request.getRole())) : new java.util.HashSet<>())
                .gender(request.getGender() != null ? request.getGender().name() : null)
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .active(true)
                .emailVerified(false)
                .build();
    }

    /**
     * Converts a {@link User} entity to a {@link UserProfileResponse} DTO.
     *
     * @param user User entity
     * @return UserProfileResponse DTO
     */
    public UserProfileResponse toResponse(User user) {
        return toProfileResponse(user);
    }

    /**
     * Converts a {@link User} entity to a {@link UserProfileResponse} DTO.
     *
     * @param user User entity
     * @return UserProfileResponse DTO
     */
    public UserProfileResponse toProfileResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .profileImage(user.getProfileImage())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .address(user.getAddress())
                .city(user.getCity())
                .state(user.getState())
                .country(user.getCountry())
                .postalCode(user.getPostalCode())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Converts a {@link User} entity to {@link AuthResponse} DTO.
     *
     * @param user User entity
     * @param token JWT token
     * @return AuthResponse DTO
     */
    public AuthResponse toAuthResponse(User user, String token) {
        if (user == null) {
            return null;
        }
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .user(toUserInfo(user))
                .build();
    }

    /**
     * Converts a {@link User} entity to {@link AuthResponse.UserInfo} summary DTO.
     *
     * @param user User entity
     * @return AuthResponse.UserInfo DTO
     */
    public AuthResponse.UserInfo toUserInfo(User user) {
        if (user == null) {
            return null;
        }
        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .active(user.getActive())
                .city(user.getCity())
                .state(user.getState())
                .build();
    }

    /**
     * Updates an existing {@link User} entity using data from an {@link UpdateProfileRequest}.
     *
     * @param request update profile request DTO
     * @param user target User entity
     */
    public void updateEntityFromRequest(UpdateProfileRequest request, User user) {
        if (request == null || user == null) {
            return;
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }
        if (request.getState() != null) {
            user.setState(request.getState());
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }
        if (request.getPostalCode() != null) {
            user.setPostalCode(request.getPostalCode());
        }
        if (request.getLatitude() != null) {
            user.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            user.setLongitude(request.getLongitude());
        }
    }
}
