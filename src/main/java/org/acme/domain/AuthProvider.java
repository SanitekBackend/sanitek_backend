package org.acme.domain;

public interface AuthProvider {
    String verifyToken(String idToken);
}
