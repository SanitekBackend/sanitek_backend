package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Temperature;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TemperatureRepository implements PanacheRepository<Temperature> {

    public Optional<Temperature> findLatestByStation(Long stationId) {
        return find("station.id", Sort.by("registeredAt").descending(), stationId)
                .firstResultOptional();
    }

    public List<Temperature> findLatestByStations(List<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) return List.of();
        return find("station.id IN ?1",
                Sort.by("registeredAt").descending(), stationIds).list();
    }
}
