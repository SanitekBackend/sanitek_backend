package org.acme.infrastructure.messaging.rabbitmq;

import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.infrastructure.messaging.events.AlertEvent;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AuditAlertConsumer {

    private static final Logger LOG = Logger.getLogger(AuditAlertConsumer.class);

    @Incoming("audit-alerts-in")
    public void onAlertEvent(JsonObject json) {
        AlertEvent event = json.mapTo(AlertEvent.class);
        LOG.infof(
                "[Alert audit] type=%s alertId=%s userId=%s municipalityId=%s occurredAt=%s",
                event.eventType(),
                event.alertId(),
                event.userId(),
                event.municipalityId(),
                event.occurredAt()
        );
    }
}
