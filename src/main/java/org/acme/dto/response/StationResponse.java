package org.acme.dto.response;

public record StationResponse(
        Long id,
        String stationShortName,
        String stationName,
        MunicipalitySummary municipality
) {}
