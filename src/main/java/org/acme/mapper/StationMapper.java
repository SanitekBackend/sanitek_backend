package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Station;
import org.acme.dto.response.MunicipalitySummary;
import org.acme.dto.response.StationResponse;

import java.util.List;

@ApplicationScoped
public class StationMapper {

    public StationResponse toResponse(Station station) {
        List<MunicipalitySummary> municipalities = station.getMunicipalities() != null
                ? station.getMunicipalities().stream()
                        .map(m -> new MunicipalitySummary(m.getId(), m.getMunicipalityName()))
                        .toList()
                : List.of();
        return new StationResponse(
                station.getId(),
                station.getStationShortName(),
                station.getStationName(),
                municipalities
        );
    }
}
