package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.EstacionMeteorologica;
import java.util.List;

// PK String (claveEstacion) — usa PanacheRepositoryBase<EstacionMeteorologica, String>
@ApplicationScoped
public class EstacionMeteorologicaRepository implements PanacheRepositoryBase<EstacionMeteorologica, String> {

    public List<EstacionMeteorologica> findByAlcaldia(Long idAlcaldia) {
        return find("alcaldia.id = ?1", idAlcaldia).list();
    }

    public List<EstacionMeteorologica> findByOrganismo(String organismo) {
        return find("organismo", organismo).list();
    }
}
