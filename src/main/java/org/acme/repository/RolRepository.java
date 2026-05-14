package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Rol;
import java.util.Optional;

@ApplicationScoped
public class RolRepository implements PanacheRepository<Rol> {

    public Optional<Rol> findByNombre(String nombre) {
        return find("nombreRol", nombre).firstResultOptional();
    }
}
