package org.acme.infrastructure.messaging.rabbitmq;

import io.vertx.core.json.JsonObject;
import org.acme.domain.entity.Alert;
import org.acme.domain.entity.Municipality;
import org.acme.domain.entity.User;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.MunicipalitySummary;
import org.acme.infrastructure.messaging.events.AlertEvent;
import org.acme.repository.AlertRepository;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("RabbitMQ — Fan-out de alertas (email, push, audit)")
class RabbitMQAlertFlowTest {

    // ── AlertEventProducer ─────────────────────────────────────────────
    @Mock Emitter<AlertEvent> emitter;
    AlertEventProducer alertProducer;

    // ── Consumidores ───────────────────────────────────────────────────
    @Mock AlertRepository alertRepository;
    AuditAlertConsumer  auditConsumer;
    EmailAlertConsumer  emailConsumer;
    PushAlertConsumer   pushConsumer;

    @BeforeEach
    void init() throws Exception {
        alertProducer = new AlertEventProducer();
        set(alertProducer, "emitter", emitter);

        auditConsumer = new AuditAlertConsumer();

        emailConsumer = new EmailAlertConsumer();
        set(emailConsumer, "alertRepository", alertRepository);

        pushConsumer = new PushAlertConsumer();
    }
    @Test
    @DisplayName("publishRiskDetected → publica evento IRSA_RISK_DETECTED con datos correctos")
    void producer_riskDetected_datos_correctos() {
        // Simula lo que hace IrsaCalculationConsumer cuando detecta riesgo HIGH
        IrsaResponse irsa = irsaResponse(1L, "Cuauhtémoc", "HIGH", 0.88f);
        alertProducer.publishRiskDetected(irsa);

        AlertEvent event = capturar();
        assertAll("Evento IRSA_RISK_DETECTED",
                () -> assertEquals("IRSA_RISK_DETECTED", event.eventType()),
                () -> assertEquals(1L, event.municipalityId()),
                () -> assertEquals("Cuauhtémoc", event.municipalityName()),
                () -> assertEquals("HIGH", event.riskLevel()),
                () -> assertEquals(0.88f, event.irsaValue()),
                () -> assertTrue(event.message().contains("HIGH")),
                () -> assertNotNull(event.occurredAt())
        );
    }

    @Test
    @DisplayName("publishAlertCreated → publica evento ALERT_CREATED con datos del alert")
    void producer_alertCreated_datos_correctos() {
        // Simula lo que hace AlertService cuando se llama POST /api/alerts
        Alert alert = buildAlert(5L, 1L, "user@sanitek.mx", 2L, "Iztapalapa",
                "AIR_QUALITY", "Alerta de calidad del aire");
        alertProducer.publishAlertCreated(alert);

        AlertEvent event = capturar();
        assertAll("Evento ALERT_CREATED",
                () -> assertEquals("ALERT_CREATED", event.eventType()),
                () -> assertEquals(5L, event.alertId()),
                () -> assertEquals("user@sanitek.mx", event.userEmail()),
                () -> assertEquals(2L, event.municipalityId()),
                () -> assertEquals("Iztapalapa", event.municipalityName()),
                () -> assertEquals("AIR_QUALITY", event.alertType())
        );
    }
    @Test
    @DisplayName("AuditConsumer → registra el evento en log (sin DB, sin excepción)")
    void audit_registra_evento() {
        // Cola: sanitek.alerts.audit — solo logging
        JsonObject json = riskJson(1L, "Cuauhtémoc", "HIGH", 0.88f);
        assertDoesNotThrow(() -> auditConsumer.onAlertEvent(json));
    }

    @Test
    @DisplayName("PushConsumer → registra push notification en log (sin excepción)")
    void push_registra_notificacion() {
        // Cola: sanitek.alerts.push — solo logging (integración push pendiente)
        JsonObject json = riskJson(1L, "Cuauhtémoc", "HIGH", 0.88f);
        assertDoesNotThrow(() -> pushConsumer.onAlertEvent(json));
    }

    @Test
    @DisplayName("EmailConsumer + ALERT_CREATED → envía al email del usuario, sin consultar DB")
    void email_alert_created_envia_al_usuario() {
        // Cola: sanitek.alerts.email — evento de alerta manual
        JsonObject json = alertJson(5L, 1L, "user@sanitek.mx", 2L, "Iztapalapa",
                "AIR_QUALITY", "Alerta activa");

        assertDoesNotThrow(() -> emailConsumer.onAlertEvent(json));
        // No consulta DB: el email del usuario ya está en el evento
        verifyNoInteractions(alertRepository);
    }

    @Test
    @DisplayName("EmailConsumer + IRSA_RISK → consulta suscriptores y notifica a cada uno")
    void email_irsa_risk_notifica_suscriptores() {
        // Simula 2 usuarios suscritos a la alcaldía 1 (vía POST /api/alerts/subscribe)
        Alert s1 = suscriptor("alice@sanitek.mx");
        Alert s2 = suscriptor("bob@sanitek.mx");
        when(alertRepository.findActiveByMunicipalityWithUser(1L)).thenReturn(List.of(s1, s2));

        JsonObject json = riskJson(1L, "Cuauhtémoc", "HIGH", 0.88f);
        emailConsumer.onAlertEvent(json);

        // Consultó la DB para los suscriptores activos
        verify(alertRepository).findActiveByMunicipalityWithUser(1L);
        // Accedió al email de cada suscriptor para la notificación
        verify(s1.getUser()).getEmail();
        verify(s2.getUser()).getEmail();
    }

    @Test
    @DisplayName("EmailConsumer + IRSA_RISK → sin suscriptores: no falla, solo log")
    void email_irsa_risk_sin_suscriptores() {
        when(alertRepository.findActiveByMunicipalityWithUser(3L)).thenReturn(List.of());

        JsonObject json = riskJson(3L, "Xochimilco", "CRITICAL", 0.95f);
        assertDoesNotThrow(() -> emailConsumer.onAlertEvent(json));
    }
    @Test
    @DisplayName("Flujo completo: IRSA_RISK_DETECTED llega a los 3 consumidores RabbitMQ")
    void flujo_completo_irsa_riesgo_llega_a_los_3_consumidores() {
        // Evento que genera el IrsaCalculationConsumer cuando detecta HIGH
        JsonObject json = riskJson(1L, "Cuauhtémoc", "HIGH", 0.90f);
        when(alertRepository.findActiveByMunicipalityWithUser(1L)).thenReturn(List.of());

        // El exchange fanout entrega el mismo mensaje a las 3 colas
        assertDoesNotThrow(() -> auditConsumer.onAlertEvent(json));
        assertDoesNotThrow(() -> emailConsumer.onAlertEvent(json));
        assertDoesNotThrow(() -> pushConsumer.onAlertEvent(json));
    }
    private IrsaResponse irsaResponse(Long municipalityId, String name, String risk, float value) {
        return new IrsaResponse(1L,
                new MunicipalitySummary(municipalityId, name),
                value, risk, false, null, Instant.now());
    }

    private Alert buildAlert(Long alertId, Long userId, String email,
                              Long munId, String munName, String type, String msg) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn(email);

        Municipality mun = mock(Municipality.class);
        when(mun.getId()).thenReturn(munId);
        when(mun.getMunicipalityName()).thenReturn(munName);

        Alert alert = mock(Alert.class);
        when(alert.getId()).thenReturn(alertId);
        when(alert.getUser()).thenReturn(user);
        when(alert.getMunicipality()).thenReturn(mun);
        when(alert.getAlertType()).thenReturn(type);
        when(alert.getMessage()).thenReturn(msg);
        return alert;
    }

    /** JsonObject para evento IRSA_RISK_DETECTED (lo que llega desde RabbitMQ). */
    private JsonObject riskJson(Long municipalityId, String name, String risk, float irsa) {
        return new JsonObject()
                .put("eventType",       "IRSA_RISK_DETECTED")
                .put("municipalityId",  municipalityId)
                .put("municipalityName", name)
                .put("alertType",       "IRSA_RISK")
                .put("riskLevel",       risk)
                .put("irsaValue",       irsa)
                .put("message",         "IRSA risk " + risk + " en " + name);
    }

    /** JsonObject para evento ALERT_CREATED (lo que llega desde RabbitMQ). */
    private JsonObject alertJson(Long alertId, Long userId, String email,
                                  Long munId, String munName, String type, String msg) {
        return new JsonObject()
                .put("eventType",       "ALERT_CREATED")
                .put("alertId",         alertId)
                .put("userId",          userId)
                .put("userEmail",       email)
                .put("municipalityId",  munId)
                .put("municipalityName", munName)
                .put("alertType",       type)
                .put("message",         msg);
    }

    private Alert suscriptor(String email) {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn(email);
        Municipality mun = mock(Municipality.class);
        Alert alert = mock(Alert.class);
        when(alert.getUser()).thenReturn(user);
        when(alert.getMunicipality()).thenReturn(mun);
        return alert;
    }

    private AlertEvent capturar() {
        ArgumentCaptor<AlertEvent> cap = ArgumentCaptor.forClass(AlertEvent.class);
        verify(emitter, atLeastOnce()).send(cap.capture());
        return cap.getValue();
    }

    private static void set(Object target, String field, Object value) throws Exception {
        var f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}
