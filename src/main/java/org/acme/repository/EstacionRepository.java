package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Estacion;
import java.util.List;

// PK String (idStation) — usa PanacheRepositoryBase<Estacion, String>
@ApplicationScoped
public class EstacionRepository implements PanacheRepositoryBase<Estacion, String> {

    public List<Estacion> findByAlcaldia(Long idAlcaldia) {
        return find("alcaldia.id = ?1", idAlcaldia).list();
    }
}
