package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Role;

import java.util.Optional;

@ApplicationScoped
public class RoleRepository implements PanacheRepository<Role> {

    public Optional<Role> findByRoleName(String roleName) {
        return find("roleName", roleName).firstResultOptional();
    }
}
