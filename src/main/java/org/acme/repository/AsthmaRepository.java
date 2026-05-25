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

    /** Registros de Asma del municipio a partir de una fecha. */
    public long countByMunicipalityAndSince(Long municipalityId, Instant since) {
        return count("municipality.id = ?1 AND registeredAt >= ?2", municipalityId, since);
    }

    /**
     * Total histórico de registros de Asma para el municipio.
     * Usado como numerador en la prevalencia del algoritmo IRSA.
     */
    public long countTotalByMunicipality(Long municipalityId) {
        return count("municipality.id = ?1", municipalityId);
    }
}
