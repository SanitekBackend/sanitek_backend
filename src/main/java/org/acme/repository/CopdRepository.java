package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Copd;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class CopdRepository implements PanacheRepository<Copd> {

    public List<Copd> findByMunicipality(Long municipalityId) {
        return find("municipality.id", municipalityId).list();
    }

    //total de registros EPOC de una alcaldia
    public long countByMunicipalityAndSince(Long municipalityId, Instant since) {
        return count("municipality.id = ?1 AND registeredAt >= ?2", municipalityId, since);
    }

    public long countByMunicipality(Long municipalityId) {
        return count("municipality.id", municipalityId);
    }
}
