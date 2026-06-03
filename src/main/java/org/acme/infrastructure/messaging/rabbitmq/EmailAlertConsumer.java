package org.acme.infrastructure.messaging.rabbitmq;

import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.Alert;
import org.acme.infrastructure.messaging.events.AlertEvent;
import org.acme.repository.AlertRepository;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class EmailAlertConsumer {

    private static final Logger LOG = Logger.getLogger(EmailAlertConsumer.class);

    @Inject
    AlertRepository alertRepository;

    @Incoming("email-alerts-in")
    @Transactional
    public void onAlertEvent(JsonObject json) {
        AlertEvent event = json.mapTo(AlertEvent.class);

        if ("IRSA_RISK_DETECTED".equals(event.eventType())) {
            notifyMunicipalitySubscribers(event);
            return;
        }

        if (event.userEmail() != null && !event.userEmail().isBlank()) {
            LOG.infof("[Email alert] to=%s type=%s message=%s", event.userEmail(), event.alertType(), event.message());
        }
    }

    private void notifyMunicipalitySubscribers(AlertEvent event) {
        List<Alert> subscribers = alertRepository.findActiveByMunicipalityWithUser(event.municipalityId());
        if (subscribers.isEmpty()) {
            LOG.infof("[Email alert] no active subscribers for municipalityId=%s", event.municipalityId());
            return;
        }

        subscribers.forEach(alert -> LOG.infof(
                "[Email alert] to=%s municipality=%s risk=%s irsa=%s",
                alert.getUser().getEmail(),
                event.municipalityName(),
                event.riskLevel(),
                event.irsaValue()
        ));
    }
}
