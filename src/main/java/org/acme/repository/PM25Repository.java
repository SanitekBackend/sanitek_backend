package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.PM25;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PM25Repository implements PanacheRepository<PM25> {

    public List<PM25> findByStation(Long stationId) {
        return find("station.id", Sort.by("registeredAt").descending(), stationId).list();
    }

    public List<PM25> findByStationsAndDateRange(List<Long> stationIds, Instant from, Instant to) {
        if (stationIds == null || stationIds.isEmpty()) return List.of();
        return find("station.id IN ?1 AND registeredAt >= ?2 AND registeredAt <= ?3",
                stationIds, from, to).list();
    }

    public Optional<Instant> findLatestRegisteredAtByStations(List<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) return Optional.empty();
        Instant latest = getEntityManager()
                .createQuery("""
                        SELECT MAX(p.registeredAt)
                        FROM PM25 p
                        WHERE p.station.id IN :stationIds
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
                        SELECT p.metricValue FROM PM25 p
                        WHERE p.station.id IN :stationIds
                        ORDER BY p.registeredAt DESC
                        """, String.class)
                .setParameter("stationIds", stationIds)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<String> findRecentValuesByStationsBefore(List<Long> stationIds, Instant before, int limit) {
        if (stationIds == null || stationIds.isEmpty() || before == null) return List.of();
        return getEntityManager()
                .createQuery("""
                        SELECT p.metricValue FROM PM25 p
                        WHERE p.station.id IN :stationIds
                          AND p.registeredAt < :before
                        ORDER BY p.registeredAt DESC
                        """, String.class)
                .setParameter("stationIds", stationIds)
                .setParameter("before", before)
                .setMaxResults(limit)
                .getResultList();
    }

    /** Devuelve los últimos {@code limit} metricValue de TODAS las estaciones (ciudad entera).
     *  Usado como fallback final cuando la alcaldía no tiene ningún dato histórico válido. */
    public List<String> findCityWideRecentValues(int limit) {
        return getEntityManager()
                .createQuery("""
                        SELECT p.metricValue FROM PM25 p
                        ORDER BY p.registeredAt DESC
                        """, String.class)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<String> findCityWideRecentValuesBefore(Instant before, int limit) {
        if (before == null) return List.of();
        return getEntityManager()
                .createQuery("""
                        SELECT p.metricValue FROM PM25 p
                        WHERE p.registeredAt < :before
                        ORDER BY p.registeredAt DESC
                        """, String.class)
                .setParameter("before", before)
                .setMaxResults(limit)
                .getResultList();
    }
}
