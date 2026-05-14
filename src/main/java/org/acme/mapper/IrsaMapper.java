package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.entity.Irsa;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.IrsaResumen;

@ApplicationScoped
public class IrsaMapper {

    @Inject
    AlcaldiaMapper alcaldiaMapper;

    public IrsaResponse toResponse(Irsa i) {
        return new IrsaResponse(
                i.id,
                alcaldiaMapper.toResumen(i.alcaldia),
                i.valorIrsa,
                i.valorIrsa != null ? i.valorIrsa * 100.0 : 0.0,
                i.nivelRiesgo,
                i.fechaCalculo,
                i.prediccionFutura,
                i.fechaPrediccion,
                i.origenCalculo
        );
    }

    public IrsaResumen toResumen(Irsa i) {
        return new IrsaResumen(i.valorIrsa, i.nivelRiesgo, i.fechaCalculo);
    }
}
