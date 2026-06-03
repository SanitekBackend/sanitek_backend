package org.acme.dto.response;

public record MunicipalityResponse(
        Long id,
        String municipalityName,
        Float socialVulnerability,
        IrsaSummary currentIrsa
) {}
