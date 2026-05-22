package org.acme.infrastructure.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.service.IrsaService;
import org.jboss.logging.Logger;

@ApplicationScoped
public class IrsaRefreshJob {

    private static final Logger LOG = Logger.getLogger(IrsaRefreshJob.class);

    @Inject IrsaService irsaService;

    // Hourly placeholder — pending integration with external APIs (SINAICA, ERA, etc.)
    @Scheduled(cron = "{irsa.refresh.cron}")
    void refresh() {
        LOG.info("[SCHEDULER] Periodic IRSA refresh — pending external API integration.");
    }

    // Daily at midnight CDMX — generates forecasts for the next 10 days
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
}
