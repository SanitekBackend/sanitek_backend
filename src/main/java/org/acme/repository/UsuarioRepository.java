package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Usuario;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    public Optional<Usuario> findByFirebaseUid(String uid) {
        return find("firebaseUid", uid).firstResultOptional();
    }

    public Optional<Usuario> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public List<Usuario> findAllByRol(Long idRol) {
        // rol.id referencia el campo Java 'id' heredado de BaseEntity en Rol
        return find("rol.id = ?1", idRol).list();
    }
}
