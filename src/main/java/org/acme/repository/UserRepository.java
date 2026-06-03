package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.User;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByFirebaseUid(String firebaseUid) {
        return find("firebaseUid", firebaseUid).firstResultOptional();
    }

    public Optional<User> findAuthenticatedByFirebaseUid(String firebaseUid) {
        return getEntityManager()
                .createQuery("""
                        SELECT u FROM User u
                        LEFT JOIN FETCH u.role
                        LEFT JOIN FETCH u.company
                        WHERE u.firebaseUid = :firebaseUid
                        """, User.class)
                .setParameter("firebaseUid", firebaseUid)
                .getResultStream()
                .findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public List<User> findByRoleName(String roleName) {
        return find("role.roleName", roleName).list();
    }

    public List<User> findByCompanyAndRoleName(Long companyId, String roleName) {
        return find("company.id = ?1 AND role.roleName = ?2", companyId, roleName).list();
    }

    public Optional<User> findActiveAdminByCompany(Long companyId) {
        return find("company.id = ?1 AND role.roleName = 'ADMIN' AND isActive = true", companyId)
                .firstResultOptional();
    }
}
