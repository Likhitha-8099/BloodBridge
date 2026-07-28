package com.bloodbridge.controller;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.AuthResponse;
import com.bloodbridge.dto.LoginRequest;
import com.bloodbridge.dto.RegisterRequest;
import com.bloodbridge.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bloodbridge.enums.Role;

/**
 * REST controller for authentication endpoints, including registration and login.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user in the system.
     *
     * @param request the registration payload containing user details
     * @return a {@link ResponseEntity} containing a success message and HTTP status 201
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {} with role: {}", request.getEmail(), request.getRole());
        log.debug("Registration request details: {}", request);
        ApiResponse response = authService.register(request);
        log.info("Registration successful for email: {}", request.getEmail());
        log.debug("Registration response payload: {}", response);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and issues a JWT token.
     *
     * @param request the login payload containing email and password
     * @return a {@link ResponseEntity} containing the JWT token, email, and role, and HTTP status 200
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        log.debug("Login request details: {}", request);
        AuthResponse response = authService.login(request);
        log.info("Login successful for email: {}, role: {}", response.getEmail(), response.getRole());
        log.debug("Login response payload: {}", response);
        return ResponseEntity.ok(response);
    }

    /**
     * Switches the active role for the authenticated user.
     *
     * @param role the new role to activate
     * @param userDetails the authenticated user details
     * @return a {@link ResponseEntity} with the new token and active role
     */
    @PostMapping("/switch-role")
    public ResponseEntity<AuthResponse> switchRole(
            @RequestParam Role role,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        log.info("Request to switch active role to {} for user {}", role, userDetails.getUsername());
        AuthResponse response = authService.switchRole(userDetails.getUsername(), role);
        log.info("Successfully switched active role to {} for user {}", role, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
