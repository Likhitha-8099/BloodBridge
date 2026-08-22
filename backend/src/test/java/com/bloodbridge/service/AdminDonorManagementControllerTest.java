package com.bloodbridge.service;

import com.bloodbridge.controller.AdminDashboardController;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.DonorProfileResponse;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminDonorManagementControllerTest {

    @Mock
    private AdminService adminService;

    @Mock
    private UserDetails adminUserDetails;

    @InjectMocks
    private AdminDashboardController adminDashboardController;

    private DonorProfileResponse sampleDonorResponse;

    @BeforeEach
    void setUp() {
        when(adminUserDetails.getUsername()).thenReturn("admin@bloodbridge.com");

        sampleDonorResponse = DonorProfileResponse.builder()
                .id(1L)
                .userId(10L)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("+919876543210")
                .bloodGroup(BloodGroup.O_POSITIVE)
                .city("Visakhapatnam")
                .state("Andhra Pradesh")
                .availableForDonation(true)
                .emergencyAvailable(true)
                .totalDonations(3)
                .build();
    }

    @Test
    @DisplayName("Admin fetches all registered donors: returns list of donors successfully")
    void testGetAllDonorsReturnsList() {
        when(adminService.getAllDonors(null, null, null))
                .thenReturn(ApiResponse.success("All registered donors retrieved successfully", List.of(sampleDonorResponse)));

        ResponseEntity<ApiResponse<List<DonorProfileResponse>>> response =
                adminDashboardController.getAllDonors(null, null, null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("John Doe", response.getBody().getData().get(0).getFullName());
        assertEquals(BloodGroup.O_POSITIVE, response.getBody().getData().get(0).getBloodGroup());
    }

    @Test
    @DisplayName("Admin fetches all registered donors when empty: returns empty list with success")
    void testGetAllDonorsReturnsEmptyList() {
        when(adminService.getAllDonors(null, null, null))
                .thenReturn(ApiResponse.success("All registered donors retrieved successfully", Collections.emptyList()));

        ResponseEntity<ApiResponse<List<DonorProfileResponse>>> response =
                adminDashboardController.getAllDonors(null, null, null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData().isEmpty());
    }

    @Test
    @DisplayName("Admin fetches donor details by ID: returns complete donor profile")
    void testGetDonorByIdReturnsDetails() {
        when(adminService.getDonorById(1L))
                .thenReturn(ApiResponse.success("Donor profile details retrieved successfully", sampleDonorResponse));

        ResponseEntity<ApiResponse<DonorProfileResponse>> response =
                adminDashboardController.getDonorById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("John Doe", response.getBody().getData().getFullName());
        assertEquals("john.doe@example.com", response.getBody().getData().getEmail());
    }

    @Test
    @DisplayName("Admin fetches non-existent donor details: propagates UserNotFoundException")
    void testGetDonorByIdNotFoundThrowsException() {
        when(adminService.getDonorById(999L))
                .thenThrow(new UserNotFoundException("Donor profile not found for ID: 999"));

        assertThrows(UserNotFoundException.class, () -> adminDashboardController.getDonorById(999L));
    }

    @Test
    @DisplayName("Admin permanently deletes donor: returns success response")
    void testAdminPermanentlyDeletesDonor() {
        when(adminService.deleteDonor(1L, "admin@bloodbridge.com"))
                .thenReturn(ApiResponse.success("Donor permanently deleted successfully"));

        ResponseEntity<ApiResponse<String>> response =
                adminDashboardController.deleteDonor(1L, adminUserDetails);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Donor permanently deleted successfully", response.getBody().getMessage());
        verify(adminService, times(1)).deleteDonor(1L, "admin@bloodbridge.com");
    }
}
