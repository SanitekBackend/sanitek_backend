package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Clima;
import java.util.List;

@ApplicationScoped
public class ClimaRepository implements PanacheRepository<Clima> {

    public List<Clima> findByAlcaldia(Long idAlcaldia) {
        return find("alcaldia.id = ?1", idAlcaldia).list();
    }

    public List<Clima> findByCatalogoClima(String clave) {
        return find("catalogoClima.clave = ?1", clave).list();
    }
}
