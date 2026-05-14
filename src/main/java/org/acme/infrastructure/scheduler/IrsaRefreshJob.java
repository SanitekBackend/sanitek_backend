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

    // Cada hora — placeholder para cuando se integren los datasource externos (SINAICA, ERA, etc.)
    @Scheduled(cron = "{irsa.refresh.cron}")
    void refresh() {
        LOG.info("[SCHEDULER] Actualización periódica del IRSA — pendiente integración con APIs externas.");
    }

    // Cada día a medianoche (hora CDMX) — genera predicciones para los próximos 10 días
    @Scheduled(cron = "{irsa.prediccion.cron}")
    void generarPrediccionesDiarias() {
        LOG.info("[SCHEDULER] Generando predicciones para los próximos 10 días...");
        try {
            irsaService.generarPredicciones(10);
            LOG.info("[SCHEDULER] Predicciones generadas correctamente.");
        } catch (Exception e) {
            LOG.errorf("[SCHEDULER] Error generando predicciones: %s", e.getMessage());
        }
    }
}
