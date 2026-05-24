package org.acme.dto.response;

public record UserResponse(
        Long id,
        String firebaseUid,
        String email,
        Boolean isActive,
        RoleResponse role
) {}
