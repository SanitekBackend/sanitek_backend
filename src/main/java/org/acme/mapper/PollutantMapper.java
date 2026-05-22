package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.NO2;
import org.acme.domain.entity.O3;
import org.acme.domain.entity.PM_2_5;
import org.acme.domain.entity.Pollutant;
import org.acme.dto.response.MeasurementResponse;
import org.acme.dto.response.MunicipalitySummary;
import org.acme.dto.response.PollutantResponse;
import org.acme.dto.response.StationResponse;

@ApplicationScoped
public class PollutantMapper {

    public PollutantResponse toPollutantResponse(Pollutant p) {
        String notation = p.getPollutantUnit() != null ? p.getPollutantUnit().getNotation() : null;
        return new PollutantResponse(p.getId(), p.getNomenclature(), p.getName(), notation);
    }

    private StationResponse toStationResponse(org.acme.domain.entity.Station station) {
        MunicipalitySummary municipality = station.getMunicipality() != null
                ? new MunicipalitySummary(
                        station.getMunicipality().getId(),
                        station.getMunicipality().getMunicipalityName(),
                        station.getMunicipality().getSocialIndex())
                : null;
        return new StationResponse(station.getId(), station.getStationShortName(), station.getStationName(), municipality);
    }

    public MeasurementResponse toMeasurementResponse(NO2 no2) {
        return new MeasurementResponse(
                no2.getId(),
                toStationResponse(no2.getStation()),
                toPollutantResponse(no2.getPollutant()),
                no2.getMetricValue(),
                no2.getRegisteredAt()
        );
    }

    public MeasurementResponse toMeasurementResponse(O3 o3) {
        return new MeasurementResponse(
                o3.getId(),
                toStationResponse(o3.getStation()),
                toPollutantResponse(o3.getPollutant()),
                o3.getMetricValue(),
                o3.getRegisteredAt()
        );
    }

    public MeasurementResponse toMeasurementResponse(PM_2_5 pm25) {
        return new MeasurementResponse(
                pm25.getId(),
                toStationResponse(pm25.getStation()),
                toPollutantResponse(pm25.getPollutant()),
                pm25.getMetricValue(),
                pm25.getRegisteredAt()
        );
    }
}
