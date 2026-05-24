package org.acme.infrastructure.auth;

import jakarta.enterprise.context.RequestScoped;
import org.acme.domain.entity.User;

@RequestScoped
public class AuthenticatedUser {

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
