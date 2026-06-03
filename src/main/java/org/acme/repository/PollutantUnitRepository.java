package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.PollutantUnit;

import java.util.Optional;

@ApplicationScoped
public class PollutantUnitRepository implements PanacheRepository<PollutantUnit> {

    public Optional<PollutantUnit> findByNotation(String notation) {
        return find("notation", notation).firstResultOptional();
    }
}
