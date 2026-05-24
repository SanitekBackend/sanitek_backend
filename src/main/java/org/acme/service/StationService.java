package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.response.StationResponse;
import org.acme.mapper.StationMapper;
import org.acme.repository.StationRepository;

import java.util.List;

@ApplicationScoped
public class StationService {

    @Inject StationRepository stationRepository;
    @Inject StationMapper stationMapper;

    public List<StationResponse> getAll() {
        return stationRepository.listAll().stream()
                .map(stationMapper::toResponse)
                .toList();
    }

    public List<StationResponse> getByMunicipality(Long municipalityId) {
        return stationRepository.findByMunicipality(municipalityId).stream()
                .map(stationMapper::toResponse)
                .toList();
    }
}
