package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Station;
import org.acme.dto.response.MunicipalitySummary;
import org.acme.dto.response.StationResponse;

@ApplicationScoped
public class StationMapper {

    public StationResponse toResponse(Station station) {
        MunicipalitySummary municipality = station.getMunicipality() != null
                ? new MunicipalitySummary(
                        station.getMunicipality().getId(),
                        station.getMunicipality().getMunicipalityName(),
                        station.getMunicipality().getSocialIndex())
                : null;
        return new StationResponse(
                station.getId(),
                station.getStationShortName(),
                station.getStationName(),
                municipality
        );
    }
}
