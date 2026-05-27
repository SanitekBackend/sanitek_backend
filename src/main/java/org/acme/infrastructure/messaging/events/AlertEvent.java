package org.acme.infrastructure.messaging.events;

import java.time.Instant;

public record AlertEvent(
        String eventType,
        Long alertId,
        Long userId,
        String userEmail,
        Long municipalityId,
        String municipalityName,
        String alertType,
        String message,
        String riskLevel,
        Float irsaValue,
        Instant occurredAt
) {
}
