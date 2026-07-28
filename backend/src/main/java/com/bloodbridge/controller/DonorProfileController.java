package com.bloodbridge.controller;

import com.bloodbridge.dto.ApiResponse;
import com.bloodbridge.dto.AvailabilityUpdateRequest;
import com.bloodbridge.dto.DonorProfileRequest;
import com.bloodbridge.dto.DonorProfileResponse;
import com.bloodbridge.dto.DonorSearchResponse;
import com.bloodbridge.enums.BloodGroup;
import com.bloodbridge.service.DonorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing donor profiles and performing donor searches.
 * Access is restricted to authenticated users with the 'DONOR' role.
 */
@RestController
@RequestMapping("/api/donors")
@PreAuthorize("hasRole('DONOR')")
@RequiredArgsConstructor
public class DonorProfileController {

    private final DonorProfileService donorProfileService;

    /**
     * Creates a donor profile for the logged-in user.
     *
     * @param request the profile details
     * @return the created profile with status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<DonorProfileResponse> createProfile(@Valid @RequestBody DonorProfileRequest request) {
        DonorProfileResponse response = donorProfileService.createProfile(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves the profile of the currently logged-in donor.
     *
     * @return the profile details with status 200 (OK)
     */
    @GetMapping("/me")
    public ResponseEntity<DonorProfileResponse> getMyProfile() {
        DonorProfileResponse response = donorProfileService.getMyProfile();
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the profile of the currently logged-in donor.
     *
     * @param request the updated profile details
     * @return the updated profile with status 200 (OK)
     */
    @PutMapping("/me")
    public ResponseEntity<DonorProfileResponse> updateProfile(@Valid @RequestBody DonorProfileRequest request) {
        DonorProfileResponse response = donorProfileService.updateProfile(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes the profile of the currently logged-in donor.
     *
     * @return a success response with status 200 (OK)
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse> deleteProfile() {
        ApiResponse response = donorProfileService.deleteProfile();
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the availability status of the currently logged-in donor.
     *
     * @param request the availability update details
     * @return the updated profile with status 200 (OK)
     */
    @PatchMapping("/availability")
    public ResponseEntity<DonorProfileResponse> updateAvailability(@Valid @RequestBody AvailabilityUpdateRequest request) {
        DonorProfileResponse response = donorProfileService.updateAvailability(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches for donors matching a specific blood group.
     *
     * @param bloodGroup the blood group to search for
     * @return a list of matching donors with status 200 (OK)
     */
    @GetMapping("/search/blood-group/{bloodGroup}")
    public ResponseEntity<List<DonorSearchResponse>> searchByBloodGroup(@PathVariable BloodGroup bloodGroup) {
        List<DonorSearchResponse> response = donorProfileService.searchByBloodGroup(bloodGroup);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches for donors located in a specific city.
     *
     * @param city the city name to search for
     * @return a list of matching donors with status 200 (OK)
     */
    @GetMapping("/search/city/{city}")
    public ResponseEntity<List<DonorSearchResponse>> searchByCity(@PathVariable String city) {
        List<DonorSearchResponse> response = donorProfileService.searchByCity(city);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all available donors in the system.
     *
     * @return a list of available donors with status 200 (OK)
     */
    @GetMapping("/search/available")
    public ResponseEntity<List<DonorSearchResponse>> getAvailableDonors() {
        List<DonorSearchResponse> response = donorProfileService.getAvailableDonors();
        return ResponseEntity.ok(response);
    }
}
