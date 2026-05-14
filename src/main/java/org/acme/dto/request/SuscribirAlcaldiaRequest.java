package org.acme.dto.request;

import jakarta.validation.constraints.*;

public record SuscribirAlcaldiaRequest(
        @NotNull @Positive Long idAlcaldia
) {}
