package org.acme.dto.response;

import java.time.Instant;

public record MeasurementResponse(
        Long id,
        StationResponse station,
        PollutantResponse pollutant,
        String metricValue,
        Instant registeredAt
) {}
