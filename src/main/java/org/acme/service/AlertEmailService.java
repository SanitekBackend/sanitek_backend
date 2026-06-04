package org.acme.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.Alert;
import org.acme.dto.response.IrsaResponse;
import org.acme.infrastructure.messaging.events.AlertEvent;
import org.acme.repository.AlertRepository;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class AlertEmailService {

    private static final Logger LOG = Logger.getLogger(AlertEmailService.class);

    @Inject AlertRepository alertRepository;
    @Inject ReactiveMailer reactiveMailer;

    public void sendAlertCreated(Alert alert) {
        if (alert == null) {
            return;
        }

        sendFromEvent(new AlertEvent(
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
        ));
    }

    public void sendRiskDetected(IrsaResponse irsa) {
        if (irsa == null) {
            return;
        }

        sendFromEvent(new AlertEvent(
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
        ));
    }

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
