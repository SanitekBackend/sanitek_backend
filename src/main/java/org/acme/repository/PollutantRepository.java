package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Pollutant;

import java.util.Optional;

@ApplicationScoped
public class PollutantRepository implements PanacheRepository<Pollutant> {

    public Optional<Pollutant> findByNomenclature(String nomenclature) {
        return find("nomenclature", nomenclature).firstResultOptional();
    }
}
