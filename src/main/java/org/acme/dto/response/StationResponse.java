package org.acme.dto.response;

import java.util.List;

public record StationResponse(
        Long id,
        String stationShortName,
        String stationName,
        List<MunicipalitySummary> municipalities
) {}
