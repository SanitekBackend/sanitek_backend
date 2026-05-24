package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Station;

import java.util.List;

@ApplicationScoped
public class StationRepository implements PanacheRepository<Station> {

    public List<Station> findByMunicipality(Long municipalityId) {
        return find("municipality.id", municipalityId).list();
    }

    public List<Long> findIdsByMunicipality(Long municipalityId) {
        return find("municipality.id", municipalityId)
                .stream()
                .map(Station::getId)
                .toList();
    }
}
