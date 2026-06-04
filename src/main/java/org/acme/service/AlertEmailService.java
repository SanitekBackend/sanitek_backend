package org.acme.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.Alert;
import org.acme.infrastructure.messaging.events.AlertEvent;
import org.acme.repository.AlertRepository;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class AlertEmailService {

    private static final Logger LOG = Logger.getLogger(AlertEmailService.class);

    @Inject AlertRepository alertRepository;
    @Inject ReactiveMailer reactiveMailer;

    public void sendFromEvent(AlertEvent event) {
        if (event == null) {
            return;
        }

        if ("IRSA_RISK_DETECTED".equals(event.eventType())) {
            notifyMunicipalitySubscribers(event);
            return;
        }

        if (event.userEmail() != null && !event.userEmail().isBlank()) {
            sendEmail(
                    event.userEmail(),
                    "Alerta Sanitaria: " + event.alertType(),
                    event.message()
            );
        }
    }

    private void notifyMunicipalitySubscribers(AlertEvent event) {
        List<Alert> subscribers = findSubscribers(event.municipalityId());

        if (subscribers.isEmpty()) {
            LOG.infof("[Email alert] no active subscribers for municipalityId=%s", event.municipalityId());
            return;
        }

        String subject = String.format("Alerta de Riesgo en %s - Nivel %s",
                event.municipalityName(), event.riskLevel());

        String body = String.format(
                "Se ha detectado un riesgo sanitario en %s.\n\n" +
                "Nivel de Riesgo: %s\n" +
                "Indice IRSA: %.2f\n\n" +
                "Por favor, tome las precauciones necesarias.",
                event.municipalityName(),
                event.riskLevel(),
                event.irsaValue()
        );

        subscribers.forEach(alert -> sendEmail(alert.getUser().getEmail(), subject, body));
    }

    @Transactional
    public List<Alert> findSubscribers(Long municipalityId) {
        return alertRepository.findActiveByMunicipalityWithUser(municipalityId);
    }

    private void sendEmail(String to, String subject, String body) {
        LOG.infof("[Email alert] Queuing to=%s | Subject=%s", to, subject);

        reactiveMailer.send(Mail.withText(to, subject, body))
                .subscribe().with(
                        success -> LOG.infof("[Email alert] Successfully sent to %s", to),
                        failure -> LOG.errorf("[Email alert] Failed to send to %s: %s", to, failure.getMessage())
                );
    }
}
