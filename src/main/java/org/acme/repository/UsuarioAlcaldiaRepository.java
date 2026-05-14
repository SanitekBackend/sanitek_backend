package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.UsuarioAlcaldia;
import org.acme.domain.entity.UsuarioAlcaldiaId;
import java.util.List;

// PK compuesta — usa PanacheRepositoryBase con UsuarioAlcaldiaId como tipo de ID
@ApplicationScoped
public class UsuarioAlcaldiaRepository implements PanacheRepositoryBase<UsuarioAlcaldia, UsuarioAlcaldiaId> {

    public List<UsuarioAlcaldia> findByUsuario(Long idUsuario) {
        return find("id.idUsuario = ?1", idUsuario).list();
    }

    public List<UsuarioAlcaldia> findByAlcaldia(Long idAlcaldia) {
        return find("id.idAlcaldia = ?1", idAlcaldia).list();
    }

    public boolean existeSuscripcion(Long idUsuario, Long idAlcaldia) {
        return count("id.idUsuario = ?1 AND id.idAlcaldia = ?2", idUsuario, idAlcaldia) > 0;
    }
}
