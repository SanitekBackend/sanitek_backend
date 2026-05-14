package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.ContaminanteParametro;
import java.util.Optional;

@ApplicationScoped
public class ContaminanteParametroRepository implements PanacheRepository<ContaminanteParametro> {

    public Optional<ContaminanteParametro> findByNomenclatura(String nomenclatura) {
        return find("nomenclatura", nomenclatura).firstResultOptional();
    }
}
