package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.MedicionContaminante;
import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class MedicionContaminanteRepository implements PanacheRepository<MedicionContaminante> {

    public List<MedicionContaminante> findByStationAndContaminante(String idStation, Long idContaminante) {
        return find("estacion.idStation = ?1 AND contaminante.id = ?2",
                Sort.by("fecha").descending(),
                idStation, idContaminante).list();
    }

    public List<MedicionContaminante> findByFechaRange(String idStation, Instant desde, Instant hasta) {
        return find("estacion.idStation = ?1 AND fecha >= ?2 AND fecha <= ?3",
                Sort.by("fecha").descending(),
                idStation, desde, hasta).list();
    }

    public List<MedicionContaminante> findByAlcaldiaAndContaminante(Long idAlcaldia, Long idContaminante, Instant desde, Instant hasta) {
        return find("estacion.alcaldia.id = ?1 AND contaminante.id = ?2 AND fecha >= ?3 AND fecha <= ?4",
                Sort.by("fecha").descending(),
                idAlcaldia, idContaminante, desde, hasta).list();
    }

    public List<MedicionContaminante> findByAlcaldiaAndFechaRange(Long idAlcaldia, Instant desde, Instant hasta) {
        return find("estacion.alcaldia.id = ?1 AND fecha >= ?2 AND fecha <= ?3",
                Sort.by("fecha").descending(),
                idAlcaldia, desde, hasta).list();
    }
}
