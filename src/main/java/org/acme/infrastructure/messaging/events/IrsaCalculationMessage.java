package org.acme.infrastructure.messaging.events;

public record IrsaCalculationMessage(String batchId, Long municipalityId) {
}
