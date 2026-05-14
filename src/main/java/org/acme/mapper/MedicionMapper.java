package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.MedicionContaminante;
import org.acme.dto.response.ContaminanteResumen;
import org.acme.dto.response.MedicionResponse;

@ApplicationScoped
public class MedicionMapper {

    public MedicionResponse toResponse(MedicionContaminante m) {
        // La notación de unidad puede ser null si el contaminante no tiene unidad asignada
        String notacion = m.contaminante.unidad != null ? m.contaminante.unidad.notacion : null;
        ContaminanteResumen contaminante = new ContaminanteResumen(
                m.contaminante.id,
                m.contaminante.nomenclatura,
                m.contaminante.nombre,
                notacion
        );
        return new MedicionResponse(m.id, m.estacion.idStation, contaminante, m.fecha, m.valorMedicion);
    }
}
