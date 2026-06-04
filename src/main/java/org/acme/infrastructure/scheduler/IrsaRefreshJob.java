package org.acme.infrastructure.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.response.IrsaResponse;
import org.acme.service.AlertEmailService;
import org.acme.service.IrsaService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class IrsaRefreshJob {

    private static final Logger LOG = Logger.getLogger(IrsaRefreshJob.class);

    @Inject IrsaService irsaService;
    @Inject AlertEmailService alertEmailService;

    @ConfigProperty(name = "irsa.alert.cooldown.hours", defaultValue = "6")
    long alertCooldownHours;

    private final Map<AlertNotificationKey, Instant> lastAlertSentAt = new ConcurrentHashMap<>();

    @Scheduled(cron = "{irsa.refresh.cron}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void refresh() {
        LOG.info("[SCHEDULER] Starting hourly IRSA observer.");

        int calculated = 0;
        int notified = 0;
        int skipped = 0;
        int failed = 0;

        for (Long municipalityId : irsaService.listAllMunicipalityIds()) {
            try {
                IrsaResponse response = irsaService.calculate(municipalityId);
                calculated++;

                if (!isHighRisk(response.riskLevel())) {
                    continue;
                }

                if (shouldNotify(response)) {
                    alertEmailService.sendRiskDetected(response);
                    notified++;
                } else {
                    skipped++;
                    LOG.debugf("[SCHEDULER] Alert cooldown active municipalityId=%d risk=%s",
                            response.municipality().id(), response.riskLevel());
                }
            } catch (Exception e) {
                failed++;
                LOG.warnf(e, "[SCHEDULER] IRSA observer failed municipalityId=%s", municipalityId);
            }
        }

        LOG.infof("[SCHEDULER] Hourly IRSA observer done calculated=%d notified=%d cooldownSkipped=%d failed=%d",
                calculated, notified, skipped, failed);
    }

    @Scheduled(cron = "{irsa.prediccion.cron}")
    void generateDailyForecasts() {
        LOG.info("[SCHEDULER] Generating forecasts for the next 10 days...");
        try {
            irsaService.generateForecasts(10);
            LOG.info("[SCHEDULER] Forecasts generated successfully.");
        } catch (Exception e) {
            LOG.errorf("[SCHEDULER] Error generating forecasts: %s", e.getMessage());
        }
    }

    private boolean isHighRisk(String riskLevel) {
        return "HIGH".equalsIgnoreCase(riskLevel) || "CRITICAL".equalsIgnoreCase(riskLevel);
    }

    private boolean shouldNotify(IrsaResponse response) {
        AlertNotificationKey key = new AlertNotificationKey(response.municipality().id(), response.riskLevel());
        Instant now = Instant.now();
        Instant previous = lastAlertSentAt.get(key);
        Duration cooldown = Duration.ofHours(Math.max(alertCooldownHours, 1));

        if (previous != null && previous.plus(cooldown).isAfter(now)) {
            return false;
        }

        lastAlertSentAt.put(key, now);
        return true;
    }

    private record AlertNotificationKey(Long municipalityId, String riskLevel) {}
}
