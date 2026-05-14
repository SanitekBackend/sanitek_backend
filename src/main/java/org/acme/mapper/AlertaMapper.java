package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.entity.Alerta;
import org.acme.dto.response.AlertaResponse;

@ApplicationScoped
public class AlertaMapper {

    @Inject
    AlcaldiaMapper alcaldiaMapper;

    public AlertaResponse toResponse(Alerta a) {
        return new AlertaResponse(
                a.id,
                alcaldiaMapper.toResumen(a.alcaldia),
                a.tipoAlerta,
                a.mensaje,
                a.estado,
                a.fechaAlerta
        );
    }
}
