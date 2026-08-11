package com.bloodbridge.service;

import com.bloodbridge.dto.request.LoginRequest;
import com.bloodbridge.dto.request.RegisterRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.AuthResponse;

/**
 * Service interface defining authentication workflows (register, login, me).
 */
public interface AuthService {

    /**
     * Registers a new user.
     *
     * @param request the registration request payload
     * @return an {@link ApiResponse} confirming registration
     */
    ApiResponse<String> register(RegisterRequest request);

    /**
     * Authenticates a user and generates a JWT.
     *
     * @param request the login credentials payload
     * @return an {@link ApiResponse} containing the generated token and user details
     */
    ApiResponse<AuthResponse> login(LoginRequest request);

    /**
     * Retrieves the user information for the currently authenticated user.
     *
     * @param email user email address
     * @return {@link ApiResponse} containing {@link AuthResponse.UserInfo} details
     */
    ApiResponse<AuthResponse.UserInfo> getCurrentUser(String email);

    /**
     * Switches active role for a multi-role or single-role user session.
     *
     * @param email user email address
     * @param targetRole target role name
     * @return {@link ApiResponse} containing updated {@link AuthResponse}
     */
    ApiResponse<AuthResponse> switchRole(String email, String targetRole);
}
