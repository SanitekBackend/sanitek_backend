package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.Irsa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class IrsaRepository implements PanacheRepository<Irsa> {

    public Optional<Irsa> findLatestByMunicipality(Long municipalityId) {
        return find("municipality.id = ?1 AND isForecast = false",
                Sort.by("createdAt").descending(),
                municipalityId).firstResultOptional();
    }

    public List<Irsa> findByRiskLevel(String riskLevel) {
        return find("riskLevel = ?1 AND isForecast = false",
                Sort.by("createdAt").descending(), riskLevel).list();
    }

    public List<Irsa> findHistoricalByMunicipality(Long municipalityId, Instant from, Instant to) {
        return find("municipality.id = ?1 AND createdAt >= ?2 AND createdAt <= ?3 AND isForecast = false",
                Sort.by("createdAt").descending(),
                municipalityId, from, to).list();
    }

    public Optional<Irsa> findByMunicipalityAndPeriod(Long municipalityId, Instant periodStart, Instant periodEnd) {
        return find("municipality.id = ?1 AND periodStart = ?2 AND periodEnd = ?3 AND isForecast = false",
                municipalityId, periodStart, periodEnd).firstResultOptional();
    }

    public List<Irsa> findHistoricalByMunicipalityPeriod(Long municipalityId, Instant from, Instant to) {
        return find("municipality.id = ?1 AND periodStart >= ?2 AND periodStart < ?3 AND isForecast = false",
                Sort.by("periodStart").ascending(),
                municipalityId, from, to).list();
    }

    public List<Irsa> findAllLatest() {
        return getEntityManager()
                .createQuery("""
                        SELECT i FROM Irsa i
                        WHERE i.isForecast = false
                          AND i.createdAt = (
                              SELECT MAX(i2.createdAt) FROM Irsa i2
                              WHERE i2.municipality = i.municipality
                                AND i2.isForecast = false
                          )
                        """, Irsa.class)
                .getResultList();
    }

    public List<Irsa> findHistoricalByRange(Instant from, Instant to) {
        return list("createdAt >= ?1 AND createdAt < ?2 AND isForecast = false",
                Sort.by("createdAt").descending(), from, to);
    }

    public List<Irsa> findForecastsByRange(Instant from, Instant to) {
        return list("forecastDate >= ?1 AND forecastDate < ?2 AND isForecast = true",
                Sort.by("createdAt").descending(), from, to);
    }

    public List<Irsa> findHistoricalNoForecast(Long municipalityId, Instant from, Instant to) {
        return list("municipality.id = ?1 AND createdAt >= ?2 AND createdAt <= ?3 AND isForecast = false",
                Sort.by("createdAt").descending(), municipalityId, from, to);
    }

    @Transactional
    public long deleteForecasts(Instant from, Instant to) {
        return delete("isForecast = true AND forecastDate >= ?1 AND forecastDate < ?2", from, to);
    }
}
