package org.acme.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(max = 128) String firebaseUid,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull @Positive Long roleId
) {}
