package org.acme.dto.response;

public record HealthSummaryResponse(
        Long municipalityId,
        String municipalityName,
        long asthmaCount,
        long copdCount,
        long pneumoniaCount,
        long smokingCount,
        long totalCases
) {}
