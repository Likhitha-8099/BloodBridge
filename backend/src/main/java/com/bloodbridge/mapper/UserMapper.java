package com.bloodbridge.mapper;

import com.bloodbridge.dto.RegisterRequest;
import com.bloodbridge.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper utility class to convert between User entities and DTOs.
 */
@Component
public class UserMapper {

    /**
     * Converts a {@link RegisterRequest} DTO to a {@link User} entity.
     * Note: The password in the returned entity is raw. It must be encrypted before database storage.
     *
     * @param request the registration request DTO
     * @return the mapped User entity
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
                .active(true)
                .build();
    }
}
