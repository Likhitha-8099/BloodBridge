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
    private final com.bloodbridge.repository.DonorProfileRepository donorProfileRepository;
    private final com.bloodbridge.repository.HospitalRepository hospitalRepository;
    private final com.bloodbridge.repository.PatientProfileRepository patientProfileRepository;
    private final com.bloodbridge.repository.NotificationPreferenceRepository notificationPreferenceRepository;
    private final com.bloodbridge.repository.DeviceTokenRepository deviceTokenRepository;
    private final com.bloodbridge.repository.MatchedEmergencyDonorRepository matchedEmergencyDonorRepository;
    private final com.bloodbridge.repository.EmergencyResponseRepository emergencyResponseRepository;
    private final com.bloodbridge.repository.MatchResultRepository matchResultRepository;
    private final com.bloodbridge.repository.NotificationRepository notificationRepository;
    private final com.bloodbridge.repository.PushDeliveryLogRepository pushDeliveryLogRepository;
    private final com.bloodbridge.repository.DonorLiveLocationRepository donorLiveLocationRepository;
    private final com.bloodbridge.repository.EmailNotificationRepository emailNotificationRepository;
    private final com.bloodbridge.repository.DonationRepository donationRepository;
    private final com.bloodbridge.repository.AuditLogRepository auditLogRepository;
    private final com.bloodbridge.service.RealtimeService realtimeService;
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

    @Override
    @Transactional
    public ApiResponse<String> deleteUser(Long id) {
        log.info("Permanently deleting user ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        String email = user.getEmail();

        // 1. Delete user-level communication/token child records
        pushDeliveryLogRepository.deleteAllByUserId(id);
        deviceTokenRepository.deleteAllByUser(user);
        notificationPreferenceRepository.deleteAllByUserId(id);
        notificationRepository.deleteAllByRecipientUserId(id);

        // 2. Clean up Donor Profile if user was a donor
        donorProfileRepository.findByUserId(id).ifPresent(dp -> {
            Long dpId = dp.getId();
            notificationRepository.unlinkDonorProfile(dpId);
            donorLiveLocationRepository.deleteAllByDonorId(dpId);
            matchedEmergencyDonorRepository.deleteAllByDonorId(dpId);
            emergencyResponseRepository.deleteAllByDonorId(dpId);
            emailNotificationRepository.deleteAllByDonorId(dpId);
            donationRepository.unlinkDonorProfile(dpId);
            matchResultRepository.deleteAllByDonorId(dpId);
            auditLogRepository.unlinkDonor(dpId);

            donorProfileRepository.delete(dp);
            donorProfileRepository.flush();
        });

        // 3. Clean up Hospital Profile if user was a hospital
        hospitalRepository.findByUserId(id).ifPresent(h -> {
            notificationRepository.unlinkHospitalProfile(h.getId());
            donationRepository.unlinkHospitalProfile(h.getId());
            matchedEmergencyDonorRepository.deleteAllByHospitalId(h.getId());
            auditLogRepository.unlinkHospital(h.getId());
            hospitalRepository.delete(h);
            hospitalRepository.flush();
        });

        // 4. Clean up Patient Profile if user was a patient
        patientProfileRepository.findByUserId(id).ifPresent(p -> {
            patientProfileRepository.delete(p);
            patientProfileRepository.flush();
        });

        userRepository.delete(user);
        userRepository.flush();

        auditLoggerService.logEvent("USER_DELETED", email, "User ID " + id + " permanently deleted");
        try {
            realtimeService.publishAdminUsersUpdate(com.bloodbridge.dto.RealtimeEventDTO.builder().eventType(com.bloodbridge.enums.RealtimeEventType.USER_DELETED).message("User deleted").build());
            realtimeService.publishAdminDashboardUpdate(com.bloodbridge.dto.RealtimeEventDTO.builder().eventType(com.bloodbridge.enums.RealtimeEventType.USER_DELETED).message("User deleted").build());
        } catch (Exception ignored) {}

        log.info("Successfully deleted user ID: {}", id);

        return ApiResponse.success("User deleted successfully");
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteDonor(Long donorId) {
        log.info("========== [START DONOR DELETION] identifier ID: {} ==========", donorId);

        com.bloodbridge.entity.DonorProfile donorProfile = donorProfileRepository.findById(donorId)
                .or(() -> donorProfileRepository.findByUserId(donorId))
                .orElse(null);

        User user = null;
        if (donorProfile != null && donorProfile.getUser() != null) {
            user = donorProfile.getUser();
        } else {
            user = userRepository.findById(donorId).orElse(null);
            if (user != null && donorProfile == null) {
                donorProfile = donorProfileRepository.findByUserId(user.getId()).orElse(null);
            }
        }

        if (donorProfile == null && user == null) {
            throw new UserNotFoundException("Donor not found with ID: " + donorId);
        }

        String userEmail = user != null ? user.getEmail() : (donorProfile != null ? donorProfile.getEmail() : "unknown");
        Long userId = user != null ? user.getId() : (donorProfile != null && donorProfile.getUser() != null ? donorProfile.getUser().getId() : null);
        Long dpId = donorProfile != null ? donorProfile.getId() : null;
        User targetUser = user != null ? user : (userId != null ? userRepository.findById(userId).orElse(null) : null);
        Long targetUid = targetUser != null ? targetUser.getId() : userId;

        log.info("[DONOR DELETION] donor user ID = {}, donor profile ID = {}, email = {}", targetUid, dpId, userEmail);

        // 1. Cleanup all user child dependencies (Push logs, device tokens, notification prefs, recipient notifications)
        if (targetUid != null) {
            log.info("[DONOR DELETION] step = deleting push delivery logs (userId={})", targetUid);
            pushDeliveryLogRepository.deleteAllByUserId(targetUid);

            if (targetUser != null) {
                log.info("[DONOR DELETION] step = deleting device tokens (userId={})", targetUid);
                deviceTokenRepository.deleteAllByUser(targetUser);
            }

            log.info("[DONOR DELETION] step = deleting notification preferences (userId={})", targetUid);
            notificationPreferenceRepository.deleteAllByUserId(targetUid);

            log.info("[DONOR DELETION] step = deleting recipient notifications (userId={})", targetUid);
            notificationRepository.deleteAllByRecipientUserId(targetUid);
        }

        // 2. Cleanup all donor profile dependencies
        if (dpId != null) {
            log.info("[DONOR DELETION] step = unlinking donor references on hospital notifications (donorId={})", dpId);
            notificationRepository.unlinkDonorProfile(dpId);

            log.info("[DONOR DELETION] step = deleting donor live locations (donorId={})", dpId);
            donorLiveLocationRepository.deleteAllByDonorId(dpId);

            log.info("[DONOR DELETION] step = deleting matched emergency donors (donorId={})", dpId);
            matchedEmergencyDonorRepository.deleteAllByDonorId(dpId);

            log.info("[DONOR DELETION] step = deleting emergency responses (donorId={})", dpId);
            emergencyResponseRepository.deleteAllByDonorId(dpId);

            log.info("[DONOR DELETION] step = deleting email notifications (donorId={})", dpId);
            emailNotificationRepository.deleteAllByDonorId(dpId);

            log.info("[DONOR DELETION] step = unlinking donations (donorId={})", dpId);
            donationRepository.unlinkDonorProfile(dpId);

            log.info("[DONOR DELETION] step = deleting match results (donorId={})", dpId);
            matchResultRepository.deleteAllByDonorId(dpId);

            log.info("[DONOR DELETION] step = unlinking audit logs (donorId={})", dpId);
            auditLogRepository.unlinkDonor(dpId);

            log.info("[DONOR DELETION] step = deleting donor profile entity (donorId={})", dpId);
            donorProfileRepository.delete(donorProfile);
            donorProfileRepository.flush();
        }

        // 3. Delete target user entity (cascade removes element collection user_roles)
        if (targetUser != null) {
            log.info("[DONOR DELETION] step = deleting user and user_roles (userId={})", targetUid);
            userRepository.delete(targetUser);
            userRepository.flush();
        }

        log.info("[DONOR DELETION] step = logging audit event and broadcasting realtime update");
        auditLoggerService.logEvent("DONOR_DELETED", userEmail, "Donor ID " + donorId + " (" + userEmail + ") permanently deleted");
        try {
            realtimeService.publishAdminUsersUpdate(com.bloodbridge.dto.RealtimeEventDTO.builder().eventType(com.bloodbridge.enums.RealtimeEventType.USER_DELETED).message("Donor deleted").build());
            realtimeService.publishAdminDashboardUpdate(com.bloodbridge.dto.RealtimeEventDTO.builder().eventType(com.bloodbridge.enums.RealtimeEventType.USER_DELETED).message("Donor deleted").build());
        } catch (Exception ignored) {}

        log.info("========== [SUCCESS DONOR DELETION] donor ID: {} ({}) ==========", donorId, userEmail);
        return ApiResponse.success("Donor permanently deleted successfully");
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }
}
