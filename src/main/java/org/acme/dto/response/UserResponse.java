package org.acme.dto.response;

public record UserResponse(
        Long id,
        String names,
        String firebaseUid,
        String email,
        Boolean isActive,
        RoleResponse role,
        CompanySummary company
) {}
