package org.acme.dto.response;

public record LoginResponse(
        String idToken,
        String refreshToken,
        Long expiresIn,
        UserResponse user
) {}
