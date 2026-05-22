package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Radiation;

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
}
