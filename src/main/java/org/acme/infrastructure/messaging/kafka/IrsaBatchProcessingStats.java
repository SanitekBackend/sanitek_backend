package org.acme.infrastructure.messaging.kafka;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class IrsaBatchProcessingStats {

    public record Snapshot(
            String batchId,
            String startedAt,
            int enqueued,
            int inProgress,
            int processed,
            int failed,
            int riskAlertsPublished,
            String lastProcessedAt,
            Long lastMunicipalityId,
            String lastRiskLevel,
            String lastError,
            boolean done
    ) {
    }

    private static final class PerBatch {
        final String batchId;
        final String startedAt = Instant.now().toString();
        final AtomicInteger enqueued = new AtomicInteger();
        final AtomicInteger inProgress = new AtomicInteger();
        final AtomicInteger processed = new AtomicInteger();
        final AtomicInteger failed = new AtomicInteger();
        final AtomicInteger riskAlertsPublished = new AtomicInteger();
        final AtomicReference<String> lastProcessedAt = new AtomicReference<>("never");
        final AtomicReference<Long> lastMunicipalityId = new AtomicReference<>();
        final AtomicReference<String> lastRiskLevel = new AtomicReference<>("-");
        final AtomicReference<String> lastError = new AtomicReference<>();

        PerBatch(String batchId) {
            this.batchId = batchId;
        }

        Snapshot snapshot() {
            int enq = enqueued.get();
            int prog = inProgress.get();
            int doneCount = processed.get();
            int failCount = failed.get();
            boolean finished = enq > 0 && prog == 0 && (doneCount + failCount) >= enq;
            return new Snapshot(
                    batchId,
                    startedAt,
                    enq,
                    prog,
                    doneCount,
                    failCount,
                    riskAlertsPublished.get(),
                    lastProcessedAt.get(),
                    lastMunicipalityId.get(),
                    lastRiskLevel.get(),
                    lastError.get(),
                    finished
            );
        }
    }

    private final ConcurrentMap<String, PerBatch> batches = new ConcurrentHashMap<>();

    public void startBatch(String batchId, int total) {
        batches.computeIfAbsent(batchId, PerBatch::new).enqueued.addAndGet(total);
    }

    public void onStart(String batchId) {
        get(batchId).inProgress.incrementAndGet();
    }

    public void onSuccess(String batchId, Long municipalityId, String riskLevel, boolean riskAlertPublished) {
        PerBatch batch = get(batchId);
        batch.inProgress.decrementAndGet();
        batch.processed.incrementAndGet();
        if (riskAlertPublished) {
            batch.riskAlertsPublished.incrementAndGet();
        }
        batch.lastProcessedAt.set(Instant.now().toString());
        batch.lastMunicipalityId.set(municipalityId);
        batch.lastRiskLevel.set(riskLevel);
        batch.lastError.set(null);
    }

    public void onFailure(String batchId, Long municipalityId, String error) {
        PerBatch batch = get(batchId);
        batch.inProgress.decrementAndGet();
        batch.failed.incrementAndGet();
        batch.lastProcessedAt.set(Instant.now().toString());
        batch.lastMunicipalityId.set(municipalityId);
        batch.lastError.set(error);
    }

    public Snapshot snapshot(String batchId) {
        PerBatch batch = batches.get(batchId);
        return batch == null ? null : batch.snapshot();
    }

    public Map<String, Snapshot> allSnapshots() {
        Map<String, Snapshot> out = new LinkedHashMap<>();
        batches.forEach((id, batch) -> out.put(id, batch.snapshot()));
        return out;
    }

    private PerBatch get(String batchId) {
        return batches.computeIfAbsent(batchId, PerBatch::new);
    }
}
