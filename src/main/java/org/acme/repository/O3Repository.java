package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.O3;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class O3Repository implements PanacheRepository<O3> {

    public List<O3> findByStation(Long stationId) {
        return find("station.id", Sort.by("registeredAt").descending(), stationId).list();
    }

    public List<O3> findByStationsAndDateRange(List<Long> stationIds, Instant from, Instant to) {
        if (stationIds == null || stationIds.isEmpty()) return List.of();
        return find("station.id IN ?1 AND registeredAt >= ?2 AND registeredAt <= ?3",
                stationIds, from, to).list();
    }

    public Optional<Instant> findLatestRegisteredAtByStations(List<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) return Optional.empty();
        Instant latest = getEntityManager()
                .createQuery("""
                        SELECT MAX(o.registeredAt)
                        FROM O3 o
                        WHERE o.station.id IN :stationIds
                        """, Instant.class)
                .setParameter("stationIds", stationIds)
                .getSingleResult();
        return Optional.ofNullable(latest);
    }
}
