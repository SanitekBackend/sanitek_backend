package org.acme.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record CreateCompanyRequest(
        @Schema(
                description = "Nombre de la empresa",
                example = "Sanitek",
                maxLength = 150
        )
        @NotBlank @Size(max = 150) String companyName
) {}
