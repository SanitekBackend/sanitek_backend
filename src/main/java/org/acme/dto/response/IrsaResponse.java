package org.acme.dto.response;

import java.time.Instant;

public record IrsaResponse(
        Long id,
        MunicipalitySummary municipality,
        Float irsaValue,
        String riskLevel,
        Boolean isForecast,
        Instant forecastDate,
        Instant calculatedAt
) {}
