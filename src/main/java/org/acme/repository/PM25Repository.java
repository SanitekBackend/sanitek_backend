package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.PM_2_5;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class PM25Repository implements PanacheRepository<PM_2_5> {

    public List<PM_2_5> findByStation(Long stationId) {
        return find("station.id", Sort.by("registeredAt").descending(), stationId).list();
    }

    public List<PM_2_5> findByStationsAndDateRange(List<Long> stationIds, Instant from, Instant to) {
        if (stationIds == null || stationIds.isEmpty()) return List.of();
        return find("station.id IN ?1 AND registeredAt >= ?2 AND registeredAt <= ?3",
                stationIds, from, to).list();
    }
}
