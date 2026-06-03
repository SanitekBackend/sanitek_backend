package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Radiation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RadiationRepository implements PanacheRepository<Radiation> {

    public Optional<Radiation> findLatestByStation(Long stationId) {
        return find("station.id", Sort.by("registeredAt").descending(), stationId)
                .firstResultOptional();
    }

    public List<Radiation> findLatestByStations(List<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) return List.of();
        return find("station.id IN ?1",
                Sort.by("registeredAt").descending(), stationIds).list();
    }

    /**
     * Devuelve las mediciones de Radiación UV de las estaciones indicadas
     * dentro del rango de fechas dado.
     * Usado por el algoritmo IRSA para calcular norm(UV).
     */
    public List<Radiation> findByStationsAndDateRange(List<Long> stationIds,
                                                      Instant from,
                                                      Instant to) {
        if (stationIds == null || stationIds.isEmpty()) return List.of();
        return find("station.id IN ?1 AND registeredAt >= ?2 AND registeredAt <= ?3",
                Sort.by("registeredAt").descending(),
                stationIds, from, to).list();
    }

    public Optional<Instant> findLatestRegisteredAtByStations(List<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) return Optional.empty();
        Instant latest = getEntityManager()
                .createQuery("""
                        SELECT MAX(r.registeredAt)
                        FROM Radiation r
                        WHERE r.station.id IN :stationIds
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
                        SELECT r.metricValue FROM Radiation r
                        WHERE r.station.id IN :stationIds
                        ORDER BY r.registeredAt DESC
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
                        SELECT r.metricValue FROM Radiation r
                        ORDER BY r.registeredAt DESC
                        """, String.class)
                .setMaxResults(limit)
                .getResultList();
    }
}
