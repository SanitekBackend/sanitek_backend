package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Pneumonia;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class PneumoniaRepository implements PanacheRepository<Pneumonia> {

    public List<Pneumonia> findByMunicipality(Long municipalityId) {
        return find("municipality.id", municipalityId).list();
    }

    /** Registros de Neumonía del municipio a partir de una fecha. */
    public long countByMunicipalityAndSince(Long municipalityId, Instant since) {
        return count("municipality.id = ?1 AND registeredAt >= ?2", municipalityId, since);
    }

    /**
     * Total histórico de registros de Neumonía para el municipio.
     * Usado como numerador en la prevalencia del algoritmo IRSA.
     */
    public long countTotalByMunicipality(Long municipalityId) {
        return count("municipality.id = ?1", municipalityId);
    }
}
