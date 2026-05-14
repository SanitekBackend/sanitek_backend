package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Alcaldia;
import org.acme.domain.entity.Irsa;
import org.acme.dto.response.AlcaldiaResumen;
import org.acme.dto.response.AlcaldiaResponse;
import org.acme.dto.response.IrsaResumen;

@ApplicationScoped
public class AlcaldiaMapper {

    public AlcaldiaResponse toResponse(Alcaldia a, Irsa irsaActual) {
        // IrsaResumen inline para evitar dependencia circular con IrsaMapper
        IrsaResumen irsaResumen = irsaActual != null
                ? new IrsaResumen(irsaActual.valorIrsa, irsaActual.nivelRiesgo, irsaActual.fechaCalculo)
                : null;
        return new AlcaldiaResponse(a.id, a.nombre, a.indiceRezagoSocial, a.nivelRezago, irsaResumen);
    }

    public AlcaldiaResumen toResumen(Alcaldia a) {
        return new AlcaldiaResumen(a.id, a.nombre, a.nivelRezago);
    }
}
