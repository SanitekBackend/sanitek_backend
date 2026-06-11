package org.acme.infrastructure.messaging.kafka;

import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.MunicipalitySummary;
import org.acme.infrastructure.messaging.events.IrsaCalculationMessage;
import org.acme.service.AlertEmailService;
import org.acme.service.IrsaService;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Kafka — Batch IRSA 3 alcaldías")
class KafkaBatchFlowTest {

    // ── dependencias del Producer ──────────────────────────────────────
    @Mock  Emitter<IrsaCalculationMessage> emitter;
    @Spy   IrsaBatchProcessingStats stats = new IrsaBatchProcessingStats();

    // ── dependencias del Consumer ──────────────────────────────────────
    @Mock  IrsaService irsaService;
    @Mock  AlertEmailService alertEmailService;

    IrsaCalculationProducer producer;
    IrsaCalculationConsumer consumer;

    static final List<Long> TRES_ALCALDIAS = List.of(1L, 2L, 3L);

    @BeforeEach
    void init() throws Exception {
        producer = new IrsaCalculationProducer();
        set(producer, "emitter", emitter);
        set(producer, "stats",   stats);

        consumer = new IrsaCalculationConsumer();
        set(consumer, "irsaService",       irsaService);
        set(consumer, "stats",             stats);
        set(consumer, "alertEmailService", alertEmailService);
    }

    @Test
    @DisplayName("publishBatch → envía 3 mensajes al topic sanitek.irsa.calculation")
    void producer_envia_3_mensajes() {
        producer.publishBatch(TRES_ALCALDIAS);

        // En producción: 3 mensajes llegan al topic Kafka "sanitek.irsa.calculation"
        // En Kafdrop (localhost:9000) aparecerán bajo ese topic
        verify(emitter, times(3)).send(any(IrsaCalculationMessage.class));
    }

    @Test
    @DisplayName("publishBatch → todos los mensajes comparten el mismo batchId")
    void producer_misma_batchId_en_los_3_mensajes() {
        ArgumentCaptor<IrsaCalculationMessage> cap =
                ArgumentCaptor.forClass(IrsaCalculationMessage.class);

        String batchId = producer.publishBatch(TRES_ALCALDIAS);
        verify(emitter, times(3)).send(cap.capture());

        // Todos los mensajes del lote llevan el mismo batchId para correlación
        Set<String> batchIds = cap.getAllValues().stream()
                .map(IrsaCalculationMessage::batchId)
                .collect(Collectors.toSet());

        assertEquals(1, batchIds.size());
        assertTrue(batchIds.contains(batchId));
    }

    @Test
    @DisplayName("publishBatch → los 3 municipalityIds están presentes en los mensajes")
    void producer_municipalityIds_correctos() {
        ArgumentCaptor<IrsaCalculationMessage> cap =
                ArgumentCaptor.forClass(IrsaCalculationMessage.class);

        producer.publishBatch(TRES_ALCALDIAS);
        verify(emitter, times(3)).send(cap.capture());

        Set<Long> ids = cap.getAllValues().stream()
                .map(IrsaCalculationMessage::municipalityId)
                .collect(Collectors.toSet());

        assertEquals(Set.of(1L, 2L, 3L), ids);
    }

    @Test
    @DisplayName("publishBatch → registra el lote con enqueued=3 en stats")
    void producer_registra_lote_en_stats() {
        String batchId = producer.publishBatch(TRES_ALCALDIAS);

        // El endpoint GET /api/irsa/batch/status/{batchId} usará este snapshot
        IrsaBatchProcessingStats.Snapshot snap = stats.snapshot(batchId);
        assertNotNull(snap);
        assertEquals(3, snap.enqueued());
    }

    @Test
    @DisplayName("consumer → llama a irsaService.calculate por cada mensaje")
    void consumer_llama_calculate_por_cada_alcaldia() throws Exception {
        when(irsaService.calculate(anyLong())).thenReturn(irsaResponse("LOW"));

        // Simula los 3 mensajes que el consumer recibe del topic Kafka
        String batchId = producer.publishBatch(TRES_ALCALDIAS);
        consumer.process(new IrsaCalculationMessage(batchId, 1L));
        consumer.process(new IrsaCalculationMessage(batchId, 2L));
        consumer.process(new IrsaCalculationMessage(batchId, 3L));

        verify(irsaService, times(3)).calculate(anyLong());
    }

    @Test
    @DisplayName("consumer → riesgo LOW/MEDIUM NO publica alerta en RabbitMQ")
    void consumer_no_alerta_riesgo_bajo() throws Exception {
        when(irsaService.calculate(1L)).thenReturn(irsaResponse("LOW"));

        String batchId = producer.publishBatch(List.of(1L));
        consumer.process(new IrsaCalculationMessage(batchId, 1L));

        // RabbitMQ exchange "sanitek.alert.events" NO recibe nada
        verify(alertEmailService, never()).sendRiskDetected(any());
    }

    @Test
    @DisplayName("consumer → riesgo HIGH publica alerta en RabbitMQ (fan-out a 3 colas)")
    void consumer_publica_alerta_riesgo_high() throws Exception {
        IrsaResponse highRisk = irsaResponse("HIGH");
        when(irsaService.calculate(1L)).thenReturn(highRisk);

        String batchId = producer.publishBatch(List.of(1L));
        consumer.process(new IrsaCalculationMessage(batchId, 1L));

        // RabbitMQ exchange "sanitek.alert.events" recibe el evento
        // Fan-out lo distribuye a: sanitek.alerts.email, sanitek.alerts.push, sanitek.alerts.audit
        verify(alertEmailService).sendRiskDetected(highRisk);
    }

    @Test
    @DisplayName("consumer → excepción en el servicio: se registra en stats, NO se propaga")
    void consumer_excepcion_registrada_en_stats() throws Exception {
        when(irsaService.calculate(1L)).thenThrow(new RuntimeException("timeout DB"));

        String batchId = producer.publishBatch(List.of(1L));

        // El consumer es resiliente: atrapa el error internamente
        assertDoesNotThrow(() -> consumer.process(new IrsaCalculationMessage(batchId, 1L)));

        assertEquals("timeout DB", stats.snapshot(batchId).lastError());
    }
    @Test
    @DisplayName("stats → batch done=true después de procesar las 3 alcaldías")
    void stats_done_despues_de_3_municipios() throws Exception {
        when(irsaService.calculate(anyLong())).thenReturn(irsaResponse("LOW"));

        String batchId = producer.publishBatch(TRES_ALCALDIAS);
        consumer.process(new IrsaCalculationMessage(batchId, 1L));
        consumer.process(new IrsaCalculationMessage(batchId, 2L));
        consumer.process(new IrsaCalculationMessage(batchId, 3L));

        IrsaBatchProcessingStats.Snapshot snap = stats.snapshot(batchId);

        // GET /api/irsa/batch/status/{batchId} mostrará: done=true, processed=3
        assertTrue(snap.done());
        assertEquals(3, snap.processed());
        assertEquals(0, snap.failed());
    }

    @Test
    @DisplayName("stats → riskAlertsPublished=1 si una alcaldía tiene riesgo HIGH")
    void stats_contabiliza_alertas_de_riesgo() throws Exception {
        when(irsaService.calculate(1L)).thenReturn(irsaResponse("LOW"));
        when(irsaService.calculate(2L)).thenReturn(irsaResponse("HIGH")); // ← dispara alerta
        when(irsaService.calculate(3L)).thenReturn(irsaResponse("MEDIUM"));

        String batchId = producer.publishBatch(TRES_ALCALDIAS);
        consumer.process(new IrsaCalculationMessage(batchId, 1L));
        consumer.process(new IrsaCalculationMessage(batchId, 2L));
        consumer.process(new IrsaCalculationMessage(batchId, 3L));

        assertEquals(1, stats.snapshot(batchId).riskAlertsPublished());
    }
    private IrsaResponse irsaResponse(String riskLevel) {
        return new IrsaResponse(1L,
                new MunicipalitySummary(1L, "Alcaldía Test"),
                0.72f, riskLevel, false, null, Instant.now());
    }

    /** Inyecta campos privados sin necesitar CDI ni @InjectMocks. */
    private static void set(Object target, String field, Object value) throws Exception {
        var f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}
