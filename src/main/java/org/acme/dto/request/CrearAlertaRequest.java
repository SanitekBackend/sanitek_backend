package org.acme.dto.request;

import jakarta.validation.constraints.*;

public record CrearAlertaRequest(
        @NotNull @Positive Long idAlcaldia,
        @NotBlank @Size(max = 100) String tipoAlerta,
        @NotBlank String mensaje
) {}
