package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Station;

import java.util.List;

@ApplicationScoped
public class StationRepository implements PanacheRepository<Station> {

    public List<Station> findByMunicipality(Long municipalityId) {
        return getEntityManager()
                .createQuery("""
                        SELECT DISTINCT s FROM Station s
                        JOIN s.municipalities m
                        WHERE m.id = :municipalityId
                        """, Station.class)
                .setParameter("municipalityId", municipalityId)
                .getResultList();
    }

    public List<Long> findIdsByMunicipality(Long municipalityId) {
        return getEntityManager()
                .createQuery("""
                        SELECT DISTINCT s.id FROM Station s
                        JOIN s.municipalities m
                        WHERE m.id = :municipalityId
                        """, Long.class)
                .setParameter("municipalityId", municipalityId)
                .getResultList();
    }
}
