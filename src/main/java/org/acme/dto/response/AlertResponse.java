package org.acme.dto.response;

import java.time.Instant;

public record AlertResponse(
        Long id,
        MunicipalitySummary municipality,
        String alertType,
        String message,
        Boolean isActive,
        Instant scheduledFor,
        Instant createdAt
) {}
