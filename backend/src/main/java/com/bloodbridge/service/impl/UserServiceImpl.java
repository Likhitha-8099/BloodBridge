package com.bloodbridge.service.impl;

import com.bloodbridge.dto.request.ChangePasswordRequest;
import com.bloodbridge.dto.request.UpdateProfileRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.UserPageResponse;
import com.bloodbridge.dto.response.UserProfileResponse;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.InvalidCredentialsException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.mapper.UserMapper;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.AuditLoggerService;
import com.bloodbridge.service.StorageService;
import com.bloodbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for User Management & Profile Foundation workflows.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final StorageService storageService;
    private final AuditLoggerService auditLoggerService;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserProfileResponse> getCurrentUser(String email) {
        log.info("Fetching user profile for email: {}", email);
        User user = findUserByEmail(email);
        UserProfileResponse response = userMapper.toProfileResponse(user);
        return ApiResponse.success("User profile retrieved successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<UserProfileResponse> updateProfile(String email, UpdateProfileRequest request) {
        log.info("Updating profile details for email: {}", email);
        User user = findUserByEmail(email);

        userMapper.updateEntityFromRequest(request, user);
        User updatedUser = userRepository.save(user);

        auditLoggerService.logEvent("PROFILE_UPDATED", email, "Profile updated successfully");
        log.info("Successfully updated profile for user ID: {}", updatedUser.getId());

        UserProfileResponse response = userMapper.toProfileResponse(updatedUser);
        return ApiResponse.success("Profile updated successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<String> changePassword(String email, ChangePasswordRequest request) {
        log.info("Processing password change request for email: {}", email);
        User user = findUserByEmail(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed: Current password mismatch for email: {}", email);
            throw new InvalidCredentialsException("Current password provided is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Password change failed: New password and confirmation mismatch for email: {}", email);
            throw new IllegalArgumentException("New password and confirmation password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditLoggerService.logEvent("PASSWORD_CHANGED", email, "Account password changed successfully");
        log.info("Successfully changed password for user ID: {}", user.getId());

        return ApiResponse.success("Password changed successfully");
    }

    @Override
    @Transactional
    public ApiResponse<String> deactivateAccount(String email) {
        log.info("Deactivating account for user email: {}", email);
        User user = findUserByEmail(email);

        user.setActive(false);
        userRepository.save(user);

        auditLoggerService.logEvent("ACCOUNT_DEACTIVATED", email, "User self-deactivated account");
        log.info("Successfully deactivated account for user ID: {}", user.getId());

        return ApiResponse.success("Account deactivated successfully");
    }

    @Override
    @Transactional
    public ApiResponse<UserProfileResponse> uploadProfileImage(String email, String imageUrl) {
        log.info("Updating profile image for user email: {}", email);
        User user = findUserByEmail(email);

        String processedUrl = storageService.storeImageUrl(imageUrl, user.getId());
        user.setProfileImage(processedUrl);
        User updatedUser = userRepository.save(user);

        auditLoggerService.logEvent("IMAGE_UPLOADED", email, "Profile image updated: " + processedUrl);
        log.info("Successfully updated profile image for user ID: {}", user.getId());

        UserProfileResponse response = userMapper.toProfileResponse(updatedUser);
        return ApiResponse.success("Profile image updated successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserPageResponse> getAllUsers(int page, int size, Role role, String query) {
        log.info("Admin fetching user list. Page: {}, Size: {}, Role: {}, Query: {}", page, size, role, query);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<User> userPage;
        if (role != null) {
            userPage = userRepository.findByRole(role, pageable);
        } else if (query != null && !query.isBlank()) {
            userPage = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<UserProfileResponse> content = userPage.getContent().stream()
                .map(userMapper::toProfileResponse)
                .collect(Collectors.toList());

        UserPageResponse response = UserPageResponse.builder()
                .content(content)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();

        return ApiResponse.success("User list retrieved successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserProfileResponse> getUserById(Long id) {
        log.info("Fetching user profile by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        UserProfileResponse response = userMapper.toProfileResponse(user);
        return ApiResponse.success("User profile retrieved successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<String> activateUser(Long id) {
        log.info("Admin activating user ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        user.setActive(true);
        userRepository.save(user);

        auditLoggerService.logEvent("ACCOUNT_ACTIVATED", user.getEmail(), "Admin activated user ID: " + id);
        log.info("Successfully activated user ID: {}", id);

        return ApiResponse.success("User account activated successfully");
    }

    @Override
    @Transactional
    public ApiResponse<String> deactivateUser(Long id) {
        log.info("Admin deactivating user ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        user.setActive(false);
        userRepository.save(user);

        auditLoggerService.logEvent("ACCOUNT_DEACTIVATED", user.getEmail(), "Admin deactivated user ID: " + id);
        log.info("Successfully deactivated user ID: {}", id);

        return ApiResponse.success("User account deactivated successfully");
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }
}
