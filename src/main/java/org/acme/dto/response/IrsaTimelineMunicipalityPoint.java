package org.acme.dto.response;

import java.time.Instant;

public record IrsaTimelineMunicipalityPoint(
        Long municipalityId,
        String municipalityName,
        int offsetDays,
        String label,
        Instant latestMeasurementAt,
        Instant windowFrom,
        Instant windowTo,
        Double irsaScore,
        String riskLevel,
        Double pollutantScore,
        Double vulnerabilityFactor,
        String status,
        String error
) {
}
