package org.acme.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateAdminRequest(
        @NotNull @Positive Long companyId,
        @NotBlank @Size(max = 150) String names,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 6, max = 128) String password
) {}
