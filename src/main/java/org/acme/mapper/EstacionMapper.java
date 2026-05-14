package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.entity.Estacion;
import org.acme.dto.response.EstacionResponse;

@ApplicationScoped
public class EstacionMapper {

    @Inject
    AlcaldiaMapper alcaldiaMapper;

    public EstacionResponse toResponse(Estacion e) {
        return new EstacionResponse(
                e.idStation,
                e.estacionNombre,
                e.latitud,
                e.longitud,
                alcaldiaMapper.toResumen(e.alcaldia)
        );
    }
}
