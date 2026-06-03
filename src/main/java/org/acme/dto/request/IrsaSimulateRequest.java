package org.acme.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record IrsaSimulateRequest(
        @NotNull
        @DecimalMin("0.0")
        Double avgNo2,
        @NotNull
        @DecimalMin("0.0")
        Double avgO3,
        @NotNull
        @DecimalMin("0.0")
        Double avgPm25,
        @NotNull
        @DecimalMin("0.0")
        Double avgUv,
        @NotNull
        @DecimalMin("0.0")
        Double avgTmp,
        @NotNull
        @Min(0)
        Long copdCount,
        @NotNull
        @Min(0)
        Long asthmaCount,
        @NotNull
        @Min(0)
        Long pneumoniaCount,
        @NotNull
        @Min(0)
        Long smokingCount
) {}
