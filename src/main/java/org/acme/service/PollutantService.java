package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.response.MeasurementResponse;
import org.acme.mapper.PollutantMapper;
import org.acme.repository.NO2Repository;
import org.acme.repository.O3Repository;
import org.acme.repository.PM25Repository;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class PollutantService {

    @Inject NO2Repository no2Repository;
    @Inject O3Repository o3Repository;
    @Inject PM25Repository pm25Repository;
    @Inject PollutantMapper pollutantMapper;

    public List<MeasurementResponse> getNo2ByStation(Long stationId, Instant from, Instant to) {
        List<Long> stationIds = List.of(stationId);
        return no2Repository.findByStationsAndDateRange(stationIds, from, to).stream()
                .map(pollutantMapper::toMeasurementResponse)
                .toList();
    }

    public List<MeasurementResponse> getO3ByStation(Long stationId, Instant from, Instant to) {
        List<Long> stationIds = List.of(stationId);
        return o3Repository.findByStationsAndDateRange(stationIds, from, to).stream()
                .map(pollutantMapper::toMeasurementResponse)
                .toList();
    }

    public List<MeasurementResponse> getPm25ByStation(Long stationId, Instant from, Instant to) {
        List<Long> stationIds = List.of(stationId);
        return pm25Repository.findByStationsAndDateRange(stationIds, from, to).stream()
                .map(pollutantMapper::toMeasurementResponse)
                .toList();
    }
}
