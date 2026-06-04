package org.acme.infrastructure.messaging.rabbitmq;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer; // Usamos la versión reactiva
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    @Inject
    ReactiveMailer reactiveMailer; // Inyectamos la versión reactiva

    @Incoming("email-alerts-in")
    // ELIMINAMOS @Transactional de aquí
    public void onAlertEvent(JsonObject json) {
        AlertEvent event = json.mapTo(AlertEvent.class);

        if ("IRSA_RISK_DETECTED".equals(event.eventType())) {
            notifyMunicipalitySubscribers(event);
            return;
        }

        if (event.userEmail() != null && !event.userEmail().isBlank()) {
            sendEmail(event.userEmail(), 
                     "Alerta Sanitaria: " + event.alertType(), 
                     event.message());
        }
    }

    private void notifyMunicipalitySubscribers(AlertEvent event) {
        // La consulta a la DB se maneja de forma independiente
        List<Alert> subscribers = findSubscribers(event.municipalityId());
        
        if (subscribers.isEmpty()) {
            LOG.infof("[Email alert] no active subscribers for municipalityId=%s", event.municipalityId());
            return;
        }

        String subject = String.format("⚠️ Alerta de Riesgo en %s - Nivel %s", 
                                       event.municipalityName(), event.riskLevel());
        
        String body = String.format(
                "Se ha detectado un riesgo sanitario en %s.\n\n" +
                "Nivel de Riesgo: %s\n" +
                "Índice IRSA: %.2f\n\n" +
                "Por favor, tome las precauciones necesarias.",
                event.municipalityName(),
                event.riskLevel(),
                event.irsaValue()
        );

        subscribers.forEach(alert -> {
            sendEmail(alert.getUser().getEmail(), subject, body);
        });
    }

    // Método auxiliar para que la transacción sea solo de lectura y rápida
    @jakarta.transaction.Transactional
    public List<Alert> findSubscribers(Long municipalityId) {
        return alertRepository.findActiveByMunicipalityWithUser(municipalityId);
    }

    private void sendEmail(String to, String subject, String body) {
        LOG.infof("[Email alert] Queuing to=%s | Subject=%s", to, subject);
        
        // Enviamos el correo de forma reactiva. No bloquea el hilo ni la transacción.
        reactiveMailer.send(Mail.withText(to, subject, body))
            .subscribe().with(
                success -> LOG.infof("[Email alert] Successfully sent to %s", to),
                failure -> LOG.errorf("[Email alert] Failed to send to %s: %s", to, failure.getMessage())
            );
    }
}
