package org.acme.dto.response;

public record MunicipalityResponse(
        Long id,
        String municipalityName,
        Float socialVulnerability,
        String socialIndex,
        IrsaSummary currentIrsa
) {}
