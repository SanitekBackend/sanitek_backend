package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Alcaldia;
import org.acme.domain.enums.NivelRezago;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AlcaldiaRepository implements PanacheRepository<Alcaldia> {

    public Optional<Alcaldia> findByNombre(String nombre) {
        return find("nombre", nombre).firstResultOptional();
    }

    public List<Alcaldia> findByNivelRezago(NivelRezago nivel) {
        return find("nivelRezago", nivel).list();
    }
}
