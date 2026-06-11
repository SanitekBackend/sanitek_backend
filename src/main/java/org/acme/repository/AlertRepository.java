package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Alert;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AlertRepository implements PanacheRepository<Alert> {

    public List<Alert> findByUser(Long userId) {
        return find("user.id", userId).list();
    }

    public List<Alert> findByMunicipality(Long municipalityId) {
        return find("municipality.id", municipalityId).list();
    }

    public List<Alert> findActiveByUser(Long userId) {
        return find("user.id = ?1 AND isActive = true", userId).list();
    }

    public List<Alert> findActiveByMunicipality(Long municipalityId) {
        return find("municipality.id = ?1 AND isActive = true", municipalityId).list();
    }

    public List<Alert> findByUserAndMunicipalityList(Long userId, Long municipalityId) {
        return find("user.id = ?1 AND municipality.id = ?2", userId, municipalityId).list();
    }

    public List<Alert> findActiveByMunicipalityWithUser(Long municipalityId) {
        return find("""
                SELECT a FROM Alert a
                JOIN FETCH a.user
                JOIN FETCH a.municipality
                WHERE a.municipality.id = ?1 AND a.isActive = true
                """, municipalityId).list();
    }

    public Optional<Alert> findByUserAndMunicipality(Long userId, Long municipalityId) {
        return find("user.id = ?1 AND municipality.id = ?2", userId, municipalityId).firstResultOptional();
    }

    public Optional<Alert> findByIdAndUser(Long alertId, Long userId) {
        return find("id = ?1 AND user.id = ?2", alertId, userId).firstResultOptional();
    }
}
