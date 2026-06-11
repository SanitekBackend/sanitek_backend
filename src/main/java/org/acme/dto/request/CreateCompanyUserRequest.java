package org.acme.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record CreateCompanyUserRequest(
        @Schema(
                description = "Nombre completo del usuario",
                example = "Ana Concepcion",
                maxLength = 150
        )
        @NotBlank @Size(max = 150) String names,
        @Schema(
                description = "Correo electronico del usuario",
                example = "ana@sanitek.com",
                format = "email",
                maxLength = 255
        )
        @NotBlank @Email @Size(max = 255) String email,
        @Schema(
                description = "Contrasena inicial del usuario",
                example = "Sanitek2026",
                minLength = 6,
                maxLength = 128
        )
        @NotBlank @Size(min = 6, max = 128) String password
) {}
