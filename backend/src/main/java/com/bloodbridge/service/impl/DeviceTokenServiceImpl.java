package com.bloodbridge.service.impl;

import com.bloodbridge.dto.request.RegisterDeviceTokenRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.DeviceTokenResponse;
import com.bloodbridge.entity.DeviceToken;
import com.bloodbridge.entity.User;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.repository.DeviceTokenRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for managing FCM Device Tokens.
 * Phase 3B.1 — Device Registration module.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<DeviceTokenResponse> registerToken(String userEmail, RegisterDeviceTokenRequest request) {
        log.info("[FCM-Reg Stage 1] Request received to register FCM token for user: {}", userEmail);

        // Stage 2: User resolved
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));
        log.info("[FCM-Reg Stage 2] User resolved successfully. ID: {}, Email: {}", user.getId(), user.getEmail());

        // Stage 3: Token validated
        if (request.getToken() == null || request.getToken().isBlank()) {
            log.warn("[FCM-Reg Stage 3] Token validation failed: FCM token string is blank.");
            return ApiResponse.error("FCM token cannot be empty.");
        }
        log.info("[FCM-Reg Stage 3] Token validated. Token preview: {}...", 
                request.getToken().substring(0, Math.min(request.getToken().length(), 15)));

        // Check if token already exists (upsert logic)
        Optional<DeviceToken> existingTokenOpt = deviceTokenRepository.findByFcmToken(request.getToken());
        DeviceToken tokenEntity;
        boolean isNewRegistration;

        LocalDateTime now = LocalDateTime.now();

        if (existingTokenOpt.isPresent()) {
            tokenEntity = existingTokenOpt.get();
            log.info("[FCM-Reg Stage 4] Existing token found (ID: {}). Updating lastSeen and metadata...", tokenEntity.getId());
            tokenEntity.setUser(user); // Re-assign user if device changed users
            tokenEntity.setLastSeen(now);
            tokenEntity.setIsActive(true);
            if (request.getBrowser() != null && !request.getBrowser().isBlank()) {
                tokenEntity.setBrowser(request.getBrowser());
            }
            if (request.getDeviceName() != null && !request.getDeviceName().isBlank()) {
                tokenEntity.setDeviceName(request.getDeviceName());
            }
            if (request.getDeviceId() != null && !request.getDeviceId().isBlank()) {
                tokenEntity.setDeviceId(request.getDeviceId());
            }
            if (request.getPlatform() != null && !request.getPlatform().isBlank()) {
                tokenEntity.setPlatform(request.getPlatform());
            }
            isNewRegistration = false;
        } else {
            log.info("[FCM-Reg Stage 4] No existing token found. Creating new DeviceToken row...");
            tokenEntity = DeviceToken.builder()
                    .user(user)
                    .fcmToken(request.getToken())
                    .platform(request.getPlatform() != null ? request.getPlatform() : "WEB")
                    .browser(request.getBrowser())
                    .deviceName(request.getDeviceName())
                    .deviceId(request.getDeviceId())
                    .isActive(true)
                    .lastSeen(now)
                    .build();
            isNewRegistration = true;
        }

        DeviceToken savedToken = deviceTokenRepository.save(tokenEntity);
        log.info("[FCM-Reg Stage 4] Device token persisted successfully. ID: {}", savedToken.getId());

        // Stage 5: Registration complete
        DeviceTokenResponse responseDto = mapToResponse(savedToken, isNewRegistration ? "Device registered successfully" : "Device token updated successfully", isNewRegistration);
        log.info("[FCM-Reg Stage 5] Registration complete for user: {}, Token ID: {}", userEmail, savedToken.getId());

        return ApiResponse.success(responseDto.getMessage(), responseDto);
    }

    @Override
    @Transactional
    public ApiResponse<DeviceTokenResponse> refreshToken(String userEmail, String oldToken, String newToken) {
        log.info("[FCM-Refresh] Refresh request for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        if (newToken == null || newToken.isBlank()) {
            return ApiResponse.error("New FCM token cannot be empty.");
        }

        LocalDateTime now = LocalDateTime.now();

        // If oldToken is provided and exists, replace it
        if (oldToken != null && !oldToken.isBlank()) {
            Optional<DeviceToken> oldEntityOpt = deviceTokenRepository.findByFcmTokenAndUser(oldToken, user);
            if (oldEntityOpt.isPresent()) {
                DeviceToken oldEntity = oldEntityOpt.get();
                oldEntity.setFcmToken(newToken);
                oldEntity.setLastSeen(now);
                oldEntity.setIsActive(true);
                DeviceToken updated = deviceTokenRepository.save(oldEntity);
                log.info("[FCM-Refresh] Successfully rotated token from oldToken to newToken for user: {}", userEmail);
                return ApiResponse.success("Token refreshed successfully", mapToResponse(updated, "Token refreshed successfully", false));
            }
        }

        // Fallback: register newToken as standard upsert
        RegisterDeviceTokenRequest req = RegisterDeviceTokenRequest.builder()
                .token(newToken)
                .platform("WEB")
                .build();
        return registerToken(userEmail, req);
    }

    @Override
    @Transactional
    public ApiResponse<Void> removeToken(String userEmail, String fcmToken) {
        log.info("[FCM-Remove] Remove token request for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        if (fcmToken == null || fcmToken.isBlank()) {
            return ApiResponse.error("FCM token cannot be empty.");
        }

        Optional<DeviceToken> tokenOpt = deviceTokenRepository.findByFcmTokenAndUser(fcmToken, user);
        if (tokenOpt.isPresent()) {
            deviceTokenRepository.deleteByFcmToken(fcmToken);
            log.info("[FCM-Remove] Successfully removed FCM token for user: {}", userEmail);
        } else {
            log.warn("[FCM-Remove] Token not found or does not belong to user: {}", userEmail);
        }

        return ApiResponse.success("Token removed successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> touchLastSeen(String userEmail, String fcmToken) {
        if (fcmToken == null || fcmToken.isBlank()) {
            return ApiResponse.error("FCM token cannot be empty.");
        }
        deviceTokenRepository.updateLastSeen(fcmToken, LocalDateTime.now());
        return ApiResponse.success("Token lastSeen updated");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<DeviceTokenResponse>> getUserActiveTokens(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        List<DeviceToken> activeTokens = deviceTokenRepository.findAllByUserAndIsActiveTrue(user);
        List<DeviceTokenResponse> responses = activeTokens.stream()
                .map(t -> mapToResponse(t, null, null))
                .collect(Collectors.toList());

        return ApiResponse.success("Active device tokens retrieved successfully", responses);
    }

    private DeviceTokenResponse mapToResponse(DeviceToken entity, String message, Boolean registered) {
        return DeviceTokenResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .platform(entity.getPlatform())
                .browser(entity.getBrowser())
                .deviceName(entity.getDeviceName())
                .deviceId(entity.getDeviceId())
                .isActive(entity.getIsActive())
                .lastSeen(entity.getLastSeen())
                .registeredAt(entity.getCreatedAt())
                .message(message)
                .registered(registered)
                .build();
    }
}
