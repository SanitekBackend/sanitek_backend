package org.acme.infrastructure.messaging.kafka;

import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.response.IrsaResponse;
import org.acme.infrastructure.messaging.events.IrsaCalculationMessage;
import org.acme.service.AlertEmailService;
import org.acme.service.IrsaService;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class IrsaCalculationConsumer {

    private static final Logger LOG = Logger.getLogger(IrsaCalculationConsumer.class);

    @Inject
    IrsaService irsaService;

    @Inject
    IrsaBatchProcessingStats stats;

    @Inject
    AlertEmailService alertEmailService;

    @Incoming("irsa-calculation-in")
    @Blocking
    public void process(IrsaCalculationMessage message) {
        String batchId = message.batchId();
        Long municipalityId = message.municipalityId();
        stats.onStart(batchId);

        try {
            LOG.infof("[Kafka IRSA] batch=%s municipalityId=%s calculating", batchId, municipalityId);
            IrsaResponse response = irsaService.calculate(municipalityId);
            boolean alertPublished = isHighRisk(response.riskLevel());
            if (alertPublished) {
                alertEmailService.sendRiskDetected(response);
            }
            stats.onSuccess(batchId, municipalityId, response.riskLevel(), alertPublished);
            LOG.infof(
                    "[Kafka IRSA] batch=%s municipalityId=%s risk=%s irsa=%s done",
                    batchId,
                    municipalityId,
                    response.riskLevel(),
                    response.irsaValue()
            );
        } catch (Exception e) {
            stats.onFailure(batchId, municipalityId, e.getMessage());
            LOG.errorf(e, "[Kafka IRSA] batch=%s municipalityId=%s failed", batchId, municipalityId);
        }
    }

    private boolean isHighRisk(String riskLevel) {
        return "HIGH".equalsIgnoreCase(riskLevel) || "CRITICAL".equalsIgnoreCase(riskLevel);
    }
}
