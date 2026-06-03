package org.acme.infrastructure.messaging.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.infrastructure.messaging.events.IrsaCalculationMessage;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class IrsaCalculationProducer {

    @Channel("irsa-calculation-out")
    Emitter<IrsaCalculationMessage> emitter;

    @Inject
    IrsaBatchProcessingStats stats;

    public String publishBatch(List<Long> municipalityIds) {
        List<Long> ids = municipalityIds == null
                ? List.of()
                : municipalityIds.stream().filter(Objects::nonNull).distinct().toList();

        String batchId = UUID.randomUUID().toString();
        stats.startBatch(batchId, ids.size());
        ids.forEach(id -> emitter.send(new IrsaCalculationMessage(batchId, id)));
        return batchId;
    }

    public String publishSingle(Long municipalityId) {
        return publishBatch(List.of(municipalityId));
    }
}
