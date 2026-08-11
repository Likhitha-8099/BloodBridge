package com.bloodbridge.service;

import com.bloodbridge.dto.request.ChangePasswordRequest;
import com.bloodbridge.dto.request.UpdateProfileRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.UserPageResponse;
import com.bloodbridge.dto.response.UserProfileResponse;
import com.bloodbridge.enums.Role;

/**
 * Service interface for User Management & Profile Foundation workflows.
 */
public interface UserService {

    /**
     * Retrieves current authenticated user profile.
     *
     * @param email user email
     * @return ApiResponse containing UserProfileResponse
     */
    ApiResponse<UserProfileResponse> getCurrentUser(String email);

    /**
     * Updates current user profile details.
     *
     * @param email user email
     * @param request profile update payload
     * @return ApiResponse containing updated UserProfileResponse
     */
    ApiResponse<UserProfileResponse> updateProfile(String email, UpdateProfileRequest request);

    /**
     * Changes user password with current password verification.
     *
     * @param email user email
     * @param request change password payload
     * @return ApiResponse confirming password update
     */
    ApiResponse<String> changePassword(String email, ChangePasswordRequest request);

    /**
     * Deactivates current user account.
     *
     * @param email user email
     * @return ApiResponse confirming account deactivation
     */
    ApiResponse<String> deactivateAccount(String email);

    /**
     * Uploads/updates profile image URL.
     *
     * @param email user email
     * @param imageUrl profile image URL string
     * @return ApiResponse containing updated UserProfileResponse
     */
    ApiResponse<UserProfileResponse> uploadProfileImage(String email, String imageUrl);

    /**
     * Admin: Retrieves a paginated list of users with optional role and query filtering.
     *
     * @param page zero-based page index
     * @param size page size
     * @param role optional role filter
     * @param query optional search query (name/email)
     * @return ApiResponse containing UserPageResponse
     */
    ApiResponse<UserPageResponse> getAllUsers(int page, int size, Role role, String query);

    /**
     * Admin: Retrieves user profile details by User ID.
     *
     * @param id user ID
     * @return ApiResponse containing UserProfileResponse
     */
    ApiResponse<UserProfileResponse> getUserById(Long id);

    /**
     * Admin: Reactivates a deactivated user account.
     *
     * @param id user ID
     * @return ApiResponse confirming account activation
     */
    ApiResponse<String> activateUser(Long id);

    /**
     * Admin: Deactivates a user account.
     *
     * @param id user ID
     * @return ApiResponse confirming account deactivation
     */
    ApiResponse<String> deactivateUser(Long id);
}
