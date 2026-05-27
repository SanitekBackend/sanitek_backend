package org.acme.infrastructure.messaging.rabbitmq;

import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.infrastructure.messaging.events.AlertEvent;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PushAlertConsumer {

    private static final Logger LOG = Logger.getLogger(PushAlertConsumer.class);

    @Incoming("push-alerts-in")
    public void onAlertEvent(JsonObject json) {
        AlertEvent event = json.mapTo(AlertEvent.class);
        LOG.infof(
                "[Push alert] type=%s municipality=%s risk=%s message=%s",
                event.eventType(),
                event.municipalityName(),
                event.riskLevel(),
                event.message()
        );
    }
}
