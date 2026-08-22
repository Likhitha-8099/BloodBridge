package com.bloodbridge.service;

import com.bloodbridge.dto.response.AdminAnalyticsResponse;
import com.bloodbridge.dto.response.AdminDashboardResponse;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.AuditLogResponse;
import com.bloodbridge.dto.response.BloodRequestResponse;
import com.bloodbridge.dto.response.DonationHistoryResponse;
import com.bloodbridge.dto.response.GlobalSearchResponse;
import com.bloodbridge.dto.response.HospitalResponse;
import com.bloodbridge.dto.response.SystemHealthResponse;
import com.bloodbridge.dto.response.UserPageResponse;
import com.bloodbridge.dto.response.UserProfileResponse;
import com.bloodbridge.dto.response.DonorProfileResponse;

import java.util.List;

/**
 * Service interface for Enterprise Admin Dashboard & System Operations Center workflows.
 */
public interface AdminService {

    /**
     * Retrieves executive dashboard KPIs summary.
     *
     * @return ApiResponse containing AdminDashboardResponse
     */
    ApiResponse<AdminDashboardResponse> getDashboard();

    /**
     * Retrieves system analytics and demand metrics.
     *
     * @return ApiResponse containing AdminAnalyticsResponse
     */
    ApiResponse<AdminAnalyticsResponse> getAnalytics();

    /**
     * Retrieves list of pending hospital registrations for admin verification.
     *
     * @return ApiResponse containing list of HospitalResponse items
     */
    ApiResponse<List<HospitalResponse>> getPendingHospitals();

    /**
     * Reviews and approves, rejects, or suspends a hospital registration.
     *
     * @param id hospital ID
     * @param status status ("APPROVED", "REJECTED", "SUSPENDED")
     * @param remarks review remarks
     * @param adminEmail admin email
     * @return ApiResponse containing updated HospitalResponse
     */
    ApiResponse<HospitalResponse> verifyHospital(Long id, String status, String remarks, String adminEmail);

    /**
     * Retrieves paginated list of all users with search filtering.
     *
     * @param page page number
     * @param size page size
     * @param query search query
     * @return ApiResponse containing UserPageResponse
     */
    ApiResponse<UserPageResponse> getAllUsers(int page, int size, String query);

    /**
     * Updates user account status (ACTIVE, DEACTIVATED, SUSPENDED).
     *
     * @param userId user ID
     * @param status target status
     * @param adminEmail admin email
     * @return ApiResponse containing UserProfileResponse
     */
    ApiResponse<UserProfileResponse> updateUserStatus(Long userId, String status, String adminEmail);

    /**
     * Monitors all system blood requests.
     *
     * @return ApiResponse containing list of BloodRequestResponse items
     */
    ApiResponse<List<BloodRequestResponse>> getAllBloodRequests();

    /**
     * Force closes or cancels an active blood request.
     *
     * @param requestId blood request ID
     * @param adminEmail admin email
     * @return ApiResponse containing updated BloodRequestResponse
     */
    ApiResponse<BloodRequestResponse> forceCloseBloodRequest(Long requestId, String adminEmail);

    /**
     * Monitors all completed/scheduled donations across hospitals.
     *
     * @return ApiResponse containing list of DonationHistoryResponse items
     */
    ApiResponse<List<DonationHistoryResponse>> getAllDonations();

    /**
     * Retrieves paginated system audit logs.
     *
     * @param page page number
     * @param size page size
     * @return ApiResponse containing list of AuditLogResponse items
     */
    ApiResponse<List<AuditLogResponse>> getAuditLogs(int page, int size);

    /**
     * Retrieves system operations, memory usage, and DB health metrics.
     *
     * @return ApiResponse containing SystemHealthResponse
     */
    ApiResponse<SystemHealthResponse> getSystemHealth();

    /**
     * Performs global search across users, hospitals, blood requests, and donations.
     *
     * @param query search keyword
     * @return ApiResponse containing GlobalSearchResponse
     */
    ApiResponse<GlobalSearchResponse> globalSearch(String query);

    /**
     * Broadcasts target notifications to specific roles or cities.
     *
     * @param adminEmail admin email
     * @param title notification title
     * @param message notification message
     * @param role target role filter (optional)
     * @param targetCity target city filter (optional)
     * @param priority priority level
     * @return ApiResponse confirmation message
     */
    ApiResponse<String> broadcastTargetNotification(String adminEmail, String title, String message, String role, String targetCity, String priority);

    /**
     * Permanently deletes a donor profile, user account, and dependent records.
     *
     * @param donorId donor profile ID or user ID
     * @param adminEmail admin email
     * @return ApiResponse confirmation message
     */
    ApiResponse<String> deleteDonor(Long donorId, String adminEmail);

    /**
     * Retrieves all registered donors with optional search, blood group, and city filters.
     */
    ApiResponse<List<DonorProfileResponse>> getAllDonors(String search, String bloodGroup, String city);

    /**
     * Retrieves detailed profile for a specific donor ID.
     */
    ApiResponse<DonorProfileResponse> getDonorById(Long id);

    /**
     * Retrieves all registered hospitals with optional search, city, and status filters.
     */
    ApiResponse<List<HospitalResponse>> getAllHospitals(String search, String city, String status);

    /**
     * Retrieves detailed profile for a specific hospital ID.
     */
    ApiResponse<HospitalResponse> getHospitalById(Long id);

    /**
     * Permanently deletes a hospital profile, user account, and dependent records.
     */
    ApiResponse<String> deleteHospital(Long hospitalId, String adminEmail);
}
