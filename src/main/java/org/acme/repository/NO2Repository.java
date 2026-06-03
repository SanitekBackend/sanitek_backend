package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.NO2;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class NO2Repository implements PanacheRepository<NO2> {

    public List<NO2> findByStation(Long stationId) {
        return find("station.id", Sort.by("registeredAt").descending(), stationId).list();
    }

    public List<NO2> findByStationsAndDateRange(List<Long> stationIds, Instant from, Instant to) {
        if (stationIds == null || stationIds.isEmpty()) return List.of();
        return find("station.id IN ?1 AND registeredAt >= ?2 AND registeredAt <= ?3",
                stationIds, from, to).list();
    }

    public Optional<Instant> findLatestRegisteredAtByStations(List<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) return Optional.empty();
        Instant latest = getEntityManager()
                .createQuery("""
                        SELECT MAX(n.registeredAt)
                        FROM NO2 n
                        WHERE n.station.id IN :stationIds
                        """, Instant.class)
                .setParameter("stationIds", stationIds)
                .getSingleResult();
        return Optional.ofNullable(latest);
    }

    /** Devuelve los últimos {@code limit} metricValue registrados (desc por fecha),
     *  sin filtrar por ventana de tiempo — usado como fallback cuando el window actual
     *  solo contiene ceros o valores inválidos. */
    public List<String> findRecentValuesByStations(List<Long> stationIds, int limit) {
        if (stationIds == null || stationIds.isEmpty()) return List.of();
        return getEntityManager()
                .createQuery("""
                        SELECT n.metricValue FROM NO2 n
                        WHERE n.station.id IN :stationIds
                        ORDER BY n.registeredAt DESC
                        """, String.class)
                .setParameter("stationIds", stationIds)
                .setMaxResults(limit)
                .getResultList();
    }

    /** Devuelve los últimos {@code limit} metricValue de TODAS las estaciones (ciudad entera).
     *  Usado como fallback final cuando la alcaldía no tiene ningún dato histórico válido. */
    public List<String> findCityWideRecentValues(int limit) {
        return getEntityManager()
                .createQuery("""
                        SELECT n.metricValue FROM NO2 n
                        ORDER BY n.registeredAt DESC
                        """, String.class)
                .setMaxResults(limit)
                .getResultList();
    }
}
