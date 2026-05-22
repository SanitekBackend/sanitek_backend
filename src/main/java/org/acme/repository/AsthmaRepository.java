package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Asthma;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class AsthmaRepository implements PanacheRepository<Asthma> {

    public List<Asthma> findByMunicipality(Long municipalityId) {
        return find("municipality.id", municipalityId).list();
    }

    public long countByMunicipalityAndSince(Long municipalityId, Instant since) {
        return count("municipality.id = ?1 AND registeredAt >= ?2", municipalityId, since);
    }
}
