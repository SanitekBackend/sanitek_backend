package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.DatoMeteorologico;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DatoMeteorologicoRepository implements PanacheRepository<DatoMeteorologico> {

    public Optional<DatoMeteorologico> findLatestByEstacion(String claveEstacion) {
        return find("estacionMeteorologica.claveEstacion = ?1",
                Sort.by("fecha").descending(),
                claveEstacion).firstResultOptional();
    }

    public List<DatoMeteorologico> findByEstacionAndFechaRange(String claveEstacion, Instant desde, Instant hasta) {
        return find("estacionMeteorologica.claveEstacion = ?1 AND fecha >= ?2 AND fecha <= ?3",
                Sort.by("fecha").descending(),
                claveEstacion, desde, hasta).list();
    }

    public List<DatoMeteorologico> findByAlcaldia(Long idAlcaldia) {
        return find("estacionMeteorologica.alcaldia.id = ?1",
                Sort.by("fecha").descending(),
                idAlcaldia).list();
    }
}
