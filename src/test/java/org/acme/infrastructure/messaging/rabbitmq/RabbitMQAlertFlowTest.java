package org.acme.infrastructure.messaging.rabbitmq;

import io.vertx.core.json.JsonObject;
import org.acme.domain.entity.Alert;
import org.acme.domain.entity.Municipality;
import org.acme.domain.entity.User;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.MunicipalitySummary;
import org.acme.infrastructure.messaging.events.AlertEvent;
import org.acme.service.AlertEmailService;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RabbitMQ alert fan-out")
class RabbitMQAlertFlowTest {

    @Mock Emitter<AlertEvent> emitter;
    @Mock AlertEmailService alertEmailService;

    private AlertEventProducer producer;
    private EmailAlertConsumer emailConsumer;

    @BeforeEach
    void setUp() throws Exception {
        producer = new AlertEventProducer();
        set(producer, "emitter", emitter);
        set(producer, "messagingEnabled", true);
        set(producer, "alertEmailService", alertEmailService);
        org.mockito.Mockito.lenient()
                .when(emitter.send(any(AlertEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        emailConsumer = new EmailAlertConsumer();
        set(emailConsumer, "alertEmailService", alertEmailService);
    }

    @Test
    void producerPublishesRiskEvent() {
        producer.publishRiskDetected(new IrsaResponse(
                1L,
                new MunicipalitySummary(2L, "Iztapalapa"),
                45.5f,
                "HIGH",
                false,
                null,
                Instant.now()
        ));

        AlertEvent event = capturePublishedEvent();
        assertEquals("IRSA_RISK_DETECTED", event.eventType());
        assertEquals(2L, event.municipalityId());
        assertEquals("HIGH", event.riskLevel());
        assertNotNull(event.occurredAt());
    }

    @Test
    void producerPublishesCreatedAlert() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(3L);
        when(user.getEmail()).thenReturn("user@sanitek.mx");
        Municipality municipality = mock(Municipality.class);
        when(municipality.getId()).thenReturn(4L);
        when(municipality.getMunicipalityName()).thenReturn("Tlalpan");
        Alert alert = mock(Alert.class);
        when(alert.getId()).thenReturn(5L);
        when(alert.getUser()).thenReturn(user);
        when(alert.getMunicipality()).thenReturn(municipality);
        when(alert.getAlertType()).thenReturn("SUBSCRIPTION");
        when(alert.getMessage()).thenReturn("Subscribed");

        producer.publishAlertCreated(alert);

        AlertEvent event = capturePublishedEvent();
        assertEquals("ALERT_CREATED", event.eventType());
        assertEquals("user@sanitek.mx", event.userEmail());
        assertEquals(4L, event.municipalityId());
    }

    @Test
    void emailConsumerDelegatesToMailerService() {
        JsonObject json = new JsonObject()
                .put("eventType", "IRSA_RISK_DETECTED")
                .put("municipalityId", 2L)
                .put("municipalityName", "Iztapalapa")
                .put("alertType", "IRSA_RISK")
                .put("riskLevel", "HIGH")
                .put("irsaValue", 45.5)
                .put("message", "High risk");

        emailConsumer.onAlertEvent(json);

        ArgumentCaptor<AlertEvent> event = ArgumentCaptor.forClass(AlertEvent.class);
        verify(alertEmailService).sendFromEvent(event.capture());
        assertEquals(2L, event.getValue().municipalityId());
    }

    @Test
    void disabledMessagingUsesDirectMailFallback() throws Exception {
        set(producer, "messagingEnabled", false);
        IrsaResponse response = new IrsaResponse(
                1L,
                new MunicipalitySummary(2L, "Iztapalapa"),
                45.5f,
                "HIGH",
                false,
                null,
                Instant.now()
        );

        assertDoesNotThrow(() -> producer.publishRiskDetected(response));

        verify(alertEmailService).sendFromEvent(any(AlertEvent.class));
    }

    private AlertEvent capturePublishedEvent() {
        ArgumentCaptor<AlertEvent> event = ArgumentCaptor.forClass(AlertEvent.class);
        verify(emitter).send(event.capture());
        return event.getValue();
    }

    private static void set(Object target, String field, Object value) throws Exception {
        var declaredField = target.getClass().getDeclaredField(field);
        declaredField.setAccessible(true);
        declaredField.set(target, value);
    }
}
