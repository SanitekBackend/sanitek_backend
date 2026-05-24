package org.acme.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAdminRequest(
        @NotBlank @Size(max = 150) String names,
        @NotBlank @Email @Size(max = 255) String email
) {}
