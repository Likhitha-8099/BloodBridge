package com.bloodbridge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for uploading document or logo URLs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Document or Logo Upload Request Payload")
public class DocumentUploadRequest {

    @NotBlank(message = "Document or logo URL is required")
    @Schema(description = "Direct accessible URL of the uploaded document or logo", example = "https://documents.bloodbridge.com/licenses/lic_99821.pdf")
    private String documentUrl;

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }
}
