package org.acme.infrastructure.messaging.rabbitmq;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.entity.Alert;
import org.acme.dto.response.IrsaResponse;
import org.acme.infrastructure.messaging.events.AlertEvent;
import org.acme.service.AlertEmailService;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Instant;

@ApplicationScoped
public class AlertEventProducer {

    private static final Logger LOG = Logger.getLogger(AlertEventProducer.class);

    @Channel("alert-events-out")
    Emitter<AlertEvent> emitter;

    @ConfigProperty(name = "messaging.enabled", defaultValue = "true")
    boolean messagingEnabled;

    @Inject
    AlertEmailService alertEmailService;

    public void publishAlertCreated(Alert alert) {
        AlertEvent event = new AlertEvent(
                "ALERT_CREATED",
                alert.getId(),
                alert.getUser().getId(),
                alert.getUser().getEmail(),
                alert.getMunicipality().getId(),
                alert.getMunicipality().getMunicipalityName(),
                alert.getAlertType(),
                alert.getMessage(),
                null,
                null,
                Instant.now()
        );
        publish(event);
    }

    public void publishRiskDetected(IrsaResponse irsa) {
        AlertEvent event = new AlertEvent(
                "IRSA_RISK_DETECTED",
                null,
                null,
                null,
                irsa.municipality().id(),
                irsa.municipality().municipalityName(),
                "IRSA_RISK",
                "IRSA risk level " + irsa.riskLevel() + " detected for " + irsa.municipality().municipalityName(),
                irsa.riskLevel(),
                irsa.irsaValue(),
                Instant.now()
        );
        publish(event);
    }

    private void publish(AlertEvent event) {
        if (!messagingEnabled) {
            LOG.debugf("[RabbitMQ] Messaging disabled, sending alert email directly type=%s municipalityId=%s",
                    event.eventType(), event.municipalityId());
            alertEmailService.sendFromEvent(event);
            return;
        }
        LOG.debugf("[RabbitMQ] Publishing alert event type=%s municipalityId=%s", event.eventType(), event.municipalityId());
        emitter.send(event);
    }
}
