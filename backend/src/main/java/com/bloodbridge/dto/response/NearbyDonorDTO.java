package com.bloodbridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing a nearby available blood donor.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Nearby Donor Summary Payload")
public class NearbyDonorDTO {

    @Schema(description = "Donor Profile ID", example = "12")
    private Long id;

    @Schema(description = "Donor Name", example = "David Miller")
    private String name;

    @Schema(description = "Blood Group", example = "O_NEGATIVE")
    private String bloodGroup;

    @Schema(description = "Distance in KM from Hospital", example = "3.5")
    private Double distanceKm;

    @Schema(description = "Availability Status", example = "AVAILABLE")
    private String availability;

    @Schema(description = "City", example = "Boston")
    private String city;

    @Schema(description = "State", example = "MA")
    private String state;
}
