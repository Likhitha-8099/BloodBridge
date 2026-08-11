package com.bloodbridge.service;

import com.bloodbridge.dto.request.RegisterDeviceTokenRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.DeviceTokenResponse;

import java.util.List;

/**
 * Service interface for managing Firebase Cloud Messaging device tokens.
 * Phase 3B.1 — Device Registration module.
 */
public interface DeviceTokenService {

    /**
     * Register or update an FCM device token for the authenticated user.
     * Upsert pattern: if the token already exists, update lastSeen and browser metadata without creating a duplicate.
     *
     * @param userEmail Email of the authenticated user
     * @param request   DTO containing token and device metadata
     * @return Standard ApiResponse with DeviceTokenResponse
     */
    ApiResponse<DeviceTokenResponse> registerToken(String userEmail, RegisterDeviceTokenRequest request);

    /**
     * Handle token rotation issued by Firebase SDK.
     * Replaces oldToken with newToken for the authenticated user.
     *
     * @param userEmail Email of the authenticated user
     * @param oldToken  Previous FCM token
     * @param newToken  Newly issued FCM token
     * @return Standard ApiResponse with DeviceTokenResponse
     */
    ApiResponse<DeviceTokenResponse> refreshToken(String userEmail, String oldToken, String newToken);

    /**
     * Deactivate or delete an FCM device token (e.g., on logout).
     *
     * @param userEmail Email of the authenticated user
     * @param fcmToken  FCM token string to remove
     * @return Standard ApiResponse
     */
    ApiResponse<Void> removeToken(String userEmail, String fcmToken);

    /**
     * Touch lastSeen timestamp for an FCM token.
     *
     * @param userEmail Email of the authenticated user
     * @param fcmToken  FCM token string
     * @return Standard ApiResponse
     */
    ApiResponse<Void> touchLastSeen(String userEmail, String fcmToken);

    /**
     * Retrieve all active device tokens for the authenticated user.
     *
     * @param userEmail Email of the authenticated user
     * @return Standard ApiResponse containing list of DeviceTokenResponse
     */
    ApiResponse<List<DeviceTokenResponse>> getUserActiveTokens(String userEmail);
}
