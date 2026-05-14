package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.ContaminanteEntity;
import org.acme.dto.response.ContaminanteAnualResponse;

@ApplicationScoped
public class ContaminanteMapper {

    public ContaminanteAnualResponse toResponse(ContaminanteEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ContaminanteAnualResponse(
            entity.municipio,
            entity.tipoCont,
            entity.valorMedicion,
            entity.anio
        );
    }
}
