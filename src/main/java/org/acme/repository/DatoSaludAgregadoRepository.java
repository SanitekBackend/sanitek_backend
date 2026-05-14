package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.DatoSaludAgregado;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DatoSaludAgregadoRepository implements PanacheRepository<DatoSaludAgregado> {

    public Optional<DatoSaludAgregado> findLatestByAlcaldia(Long idAlcaldia) {
        return find("alcaldia.id = ?1 ORDER BY anio DESC, mes DESC", idAlcaldia)
                .firstResultOptional();
    }

    public List<DatoSaludAgregado> findByAlcaldia(Long idAlcaldia) {
        return list("alcaldia.id = ?1 ORDER BY anio ASC, mes ASC", idAlcaldia);
    }

    public List<DatoSaludAgregado> findByAnio(int anio) {
        return list("anio = ?1 ORDER BY alcaldia.id ASC, mes ASC", anio);
    }

    public List<DatoSaludAgregado> findByAlcaldiaAndAnio(Long idAlcaldia, int anio) {
        return list("alcaldia.id = ?1 AND anio = ?2 ORDER BY mes ASC", idAlcaldia, anio);
    }

    public void deleteByAnio(int anio) {
        delete("anio = ?1", anio);
    }
}
