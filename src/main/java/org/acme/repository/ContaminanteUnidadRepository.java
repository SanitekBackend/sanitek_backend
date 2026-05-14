package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.ContaminanteUnidad;
import java.util.Optional;

@ApplicationScoped
public class ContaminanteUnidadRepository implements PanacheRepository<ContaminanteUnidad> {

    public Optional<ContaminanteUnidad> findByNotacion(String notacion) {
        return find("notacion", notacion).firstResultOptional();
    }
}
