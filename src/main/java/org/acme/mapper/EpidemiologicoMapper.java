package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.entity.Epidemiologico;
import org.acme.dto.response.EpidemiologicoResponse;

@ApplicationScoped
public class EpidemiologicoMapper {

    @Inject
    AlcaldiaMapper alcaldiaMapper;

    public EpidemiologicoResponse toResponse(Epidemiologico e) {
        return new EpidemiologicoResponse(
                e.id,
                alcaldiaMapper.toResumen(e.alcaldia),
                e.fechaIngreso,
                e.edad,
                e.epoc,
                e.asma,
                e.tabaquismo,
                e.resultadoPcr
        );
    }
}
