package org.acme.dto.request;

import jakarta.validation.constraints.*;

public record RegistroUsuarioRequest(
        @NotBlank @Size(max = 128) String firebaseUid,
        @NotBlank @Email String email,
        @NotNull Long idRol
) {}
