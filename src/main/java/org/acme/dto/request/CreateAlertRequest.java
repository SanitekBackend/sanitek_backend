package org.acme.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateAlertRequest(
        @NotNull @Positive Long municipalityId,
        @NotBlank @Size(max = 100) String alertType,
        @NotBlank String message
) {}
