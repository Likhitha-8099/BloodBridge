package com.bloodbridge.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Safe, sanitized context DTO injected into AI prompt based strictly on
 * the authenticated user's role and authorization level.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAiContextDTO {

    private String role;
    private String fullName;
    private String email;
    private String currentPage;

    /**
     * Role-authorized contextual data (e.g. Donor blood group & availability,
     * Hospital verification status, Patient profile details, or Admin system totals).
     */
    private Map<String, Object> roleDetails;

    /**
     * Request-specific context if the user is authorized to view a given requestId.
     */
    private Map<String, Object> activeRequestContext;
}
