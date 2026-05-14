package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.entity.ContaminanteEntity;
import org.acme.dto.response.ContaminanteAnualResponse;
import org.acme.mapper.ContaminanteMapper;
import org.acme.repository.ContaminanteRepository;

import java.util.List;

@ApplicationScoped
public class ContaminanteService {

    @Inject
    ContaminanteRepository repository;

    @Inject
    ContaminanteMapper mapper;

    public List<ContaminanteAnualResponse> getDatosAnuales(String municipio, Integer anio) {
        List<ContaminanteEntity> entities;
        if (municipio != null && anio != null) {
            entities = repository.findByMunicipioAndAnio(municipio, anio);
        } else if (anio != null) {
            entities = repository.findByAnio(anio);
        } else {
            entities = repository.listAll();
        }

        return entities.stream()
                .map(mapper::toResponse)
                .toList();
    }
}
