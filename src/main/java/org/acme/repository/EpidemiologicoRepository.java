package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Epidemiologico;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class EpidemiologicoRepository implements PanacheRepository<Epidemiologico> {

    public List<Epidemiologico> findByAlcaldiaAndFecha(Long idAlcaldia, LocalDate fecha) {
        return find("alcaldia.id = ?1 AND fechaIngreso = ?2", idAlcaldia, fecha).list();
    }

    public List<Epidemiologico> findByAlcaldia(Long idAlcaldia) {
        return find("alcaldia.id = ?1",
                Sort.by("fechaIngreso").descending(),
                idAlcaldia).list();
    }

    public List<Epidemiologico> findByAlcaldiaAndRangoFecha(Long idAlcaldia, LocalDate desde, LocalDate hasta) {
        return find("alcaldia.id = ?1 AND fechaIngreso >= ?2 AND fechaIngreso <= ?3",
                Sort.by("fechaIngreso").descending(),
                idAlcaldia, desde, hasta).list();
    }

    public long contarPorCondicion(Long idAlcaldia, String condicion) {
        return switch (condicion.toLowerCase()) {
            case "epoc"       -> count("alcaldia.id = ?1 AND epoc = true", idAlcaldia);
            case "asma"       -> count("alcaldia.id = ?1 AND asma = true", idAlcaldia);
            case "tabaquismo" -> count("alcaldia.id = ?1 AND tabaquismo = true", idAlcaldia);
            default -> 0L;
        };
    }
}
