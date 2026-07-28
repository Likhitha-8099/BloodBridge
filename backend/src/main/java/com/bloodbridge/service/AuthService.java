package com.bloodbridge.service;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.AuthResponse;
import com.bloodbridge.dto.LoginRequest;
import com.bloodbridge.dto.RegisterRequest;

/**
 * Service interface defining authentication workflows (register and login).
 */
public interface AuthService {

    /**
     * Registers a new user.
     *
     * @param request the registration request details
     * @return an {@link ApiResponse} confirming registration
     */
    ApiResponse register(RegisterRequest request);

    /**
     * Authenticates a user and generates a JWT.
     *
     * @param request the login credentials
     * @return an {@link AuthResponse} containing the generated token and user details
     */
    AuthResponse login(LoginRequest request);

    /**
     * Switches the active role for a user and generates a new JWT.
     *
     * @param email the user email
     * @param newRole the new role to activate
     * @return an {@link AuthResponse} containing the new token and updated user details
     */
    AuthResponse switchRole(String email, com.bloodbridge.enums.Role newRole);
}
