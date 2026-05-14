package org.acme.dto.request;

import jakarta.validation.constraints.*;

public record ActualizarRolRequest(
        @NotNull @Positive Long idRol
) {}
