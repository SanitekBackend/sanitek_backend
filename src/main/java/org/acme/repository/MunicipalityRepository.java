package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Municipality;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MunicipalityRepository implements PanacheRepository<Municipality> {

    public Optional<Municipality> findByName(String name) {
        return find("municipalityName", name).firstResultOptional();
    }

    public List<Long> listAllIds() {
        return getEntityManager()
                .createQuery("SELECT m.id FROM Municipality m ORDER BY m.id", Long.class)
                .getResultList();
    }

}
