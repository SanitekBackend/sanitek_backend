package org.acme.infrastructure.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.AuthProvider;
import org.acme.exception.AppException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class FirebaseAuthProvider implements AuthProvider {

    @ConfigProperty(name = "firebase.enabled", defaultValue = "true")
    boolean enabled;

    @Override
    public String verifyToken(String idToken) {
        if (!enabled) {
            throw AppException.forbidden("Autenticación Firebase no habilitada en este entorno");
        }
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken).getUid();
        } catch (FirebaseAuthException e) {
            throw AppException.forbidden("Token de autenticación inválido o expirado");
        }
    }
}
