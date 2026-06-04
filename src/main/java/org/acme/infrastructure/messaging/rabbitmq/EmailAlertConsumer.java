package org.acme.infrastructure.messaging.rabbitmq;

import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.infrastructure.messaging.events.AlertEvent;
import org.acme.service.AlertEmailService;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailAlertConsumer {

    private static final Logger LOG = Logger.getLogger(EmailAlertConsumer.class);

    @Inject
    AlertEmailService alertEmailService;

    @Incoming("email-alerts-in")
    public void onAlertEvent(JsonObject json) {
        AlertEvent event = json.mapTo(AlertEvent.class);
        LOG.debugf("[Email alert] Consuming event type=%s municipalityId=%s",
                event.eventType(), event.municipalityId());
        alertEmailService.sendFromEvent(event);
    }
}
