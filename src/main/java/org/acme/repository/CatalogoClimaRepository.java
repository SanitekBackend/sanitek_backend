package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.CatalogoClima;
import java.util.Optional;

// PK String (clave) — usa PanacheRepositoryBase<CatalogoClima, String>
@ApplicationScoped
public class CatalogoClimaRepository implements PanacheRepositoryBase<CatalogoClima, String> {

    public Optional<CatalogoClima> findByTipoClima(String tipoClima) {
        return find("tipoClima", tipoClima).firstResultOptional();
    }
}
