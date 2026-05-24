package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.entity.User;
import org.acme.exception.AppException;
import org.acme.infrastructure.auth.AuthenticatedUser;

@ApplicationScoped
public class CurrentUserService {

    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    @Inject AuthenticatedUser authenticatedUser;

    public User getCurrentUser() {
        User user = authenticatedUser.getUser();
        if (user == null) {
            throw AppException.forbidden("Authenticated user is required");
        }
        return user;
    }

    public User requireSuperAdmin() {
        User user = getCurrentUser();
        requireRole(user, ROLE_SUPER_ADMIN);
        return user;
    }

    public User requireAdmin() {
        User user = getCurrentUser();
        requireRole(user, ROLE_ADMIN);
        if (user.getCompany() == null) {
            throw AppException.forbidden("Admin user is not assigned to a company");
        }
        return user;
    }

    private void requireRole(User user, String roleName) {
        if (user.getRole() == null || !roleName.equals(user.getRole().getRoleName())) {
            throw AppException.forbidden("User does not have permission for this operation");
        }
    }
}
