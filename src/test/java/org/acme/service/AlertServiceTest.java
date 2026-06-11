package org.acme.service;

import org.acme.domain.entity.Alert;
import org.acme.domain.entity.Municipality;
import org.acme.domain.entity.User;
import org.acme.dto.request.CreateAlertRequest;
import org.acme.dto.response.AlertResponse;
import org.acme.dto.response.MunicipalitySummary;
import org.acme.exception.AppException;
import org.acme.infrastructure.messaging.rabbitmq.AlertEventProducer;
import org.acme.mapper.AlertMapper;
import org.acme.repository.AlertRepository;
import org.acme.repository.MunicipalityRepository;
import org.acme.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService — gestión de alertas")
class AlertServiceTest {

    @Mock AlertRepository        alertRepository;
    @Mock UserRepository         userRepository;
    @Mock MunicipalityRepository municipalityRepository;
    @Mock AlertMapper            alertMapper;
    @Mock AlertEventProducer     alertEventProducer;

    @InjectMocks AlertService service;

    private static final Long USER_ID = 1L;
    private static final Long MUN_ID  = 2L;
    private static final Long ALERT_ID = 10L;

    private User         user;
    private Municipality mun;

    @BeforeEach
    void setUp() throws Exception {
        user = new User();
        user.setEmail("test@example.com");
        user.setFirebaseUid("uid_test");
        setField(user, "id", USER_ID);

        mun = new Municipality();
        mun.setMunicipalityName("Coyoacán");
        setField(mun, "id", MUN_ID);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create → usuario no encontrado → lanza AppException 404")
    void create_usuarioNoExiste_lanzaNotFound() {
        when(userRepository.findByIdOptional(USER_ID)).thenReturn(Optional.empty());
        CreateAlertRequest req = new CreateAlertRequest(MUN_ID, "RISK_HIGH", "Riesgo alto detectado");

        AppException ex = assertThrows(AppException.class,
                () -> service.create(USER_ID, req));

        assertEquals(jakarta.ws.rs.core.Response.Status.NOT_FOUND, ex.getStatus());
        // flush() nunca se llama si no se llegó a persist → verifica indirectamente
        verify(alertRepository, never()).flush();
    }

    @Test
    @DisplayName("create → municipio no encontrado → lanza AppException 404")
    void create_municipioNoExiste_lanzaNotFound() {
        when(userRepository.findByIdOptional(USER_ID)).thenReturn(Optional.of(user));
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.empty());
        CreateAlertRequest req = new CreateAlertRequest(MUN_ID, "INFO", "Mensaje");

        AppException ex = assertThrows(AppException.class,
                () -> service.create(USER_ID, req));

        assertEquals(jakarta.ws.rs.core.Response.Status.NOT_FOUND, ex.getStatus());
        verify(alertRepository, never()).flush();
    }

    @Test
    @DisplayName("create → datos válidos → persiste alerta, publica evento y devuelve response")
    void create_datosValidos_persisteYPublica() throws Exception {
        when(userRepository.findByIdOptional(USER_ID)).thenReturn(Optional.of(user));
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        CreateAlertRequest req = new CreateAlertRequest(MUN_ID, "RISK_HIGH", "Alerta de riesgo");

        AlertResponse expected = alertResponse(ALERT_ID, true);
        when(alertMapper.toResponse(any(Alert.class))).thenReturn(expected);

        AlertResponse actual = service.create(USER_ID, req);

        assertEquals(expected, actual);
        // flush() confirma que persist() fue llamado antes
        verify(alertRepository).flush();
        verify(alertEventProducer).publishAlertCreated(any(Alert.class));
    }

    @Test
    @DisplayName("create → alerta se crea con isActive=true y alertType correcto")
    void create_alertaCreadaActiva() throws Exception {
        when(userRepository.findByIdOptional(USER_ID)).thenReturn(Optional.of(user));
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(alertMapper.toResponse(any())).thenReturn(alertResponse(ALERT_ID, true));
        CreateAlertRequest req = new CreateAlertRequest(MUN_ID, "INFO", "Msg");

        service.create(USER_ID, req);

        // Capturamos la entidad publicada al event producer (unívoco, sin sobrecargas)
        ArgumentCaptor<Alert> cap = ArgumentCaptor.forClass(Alert.class);
        verify(alertEventProducer).publishAlertCreated(cap.capture());
        assertTrue(cap.getValue().getIsActive());
        assertEquals("INFO", cap.getValue().getAlertType());
    }

    // ── subscribe ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("subscribe → usuario no encontrado → lanza AppException 404")
    void subscribe_usuarioNoExiste_lanzaNotFound() {
        when(userRepository.findByIdOptional(USER_ID)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> service.subscribe(USER_ID, MUN_ID));
    }

    @Test
    @DisplayName("subscribe → alerta existente → la reactiva y publica evento")
    void subscribe_alertaExistente_reactivaYPublica() throws Exception {
        Alert existente = alert(ALERT_ID, user, mun, "SUBSCRIPTION", false);
        AlertResponse expected = alertResponse(ALERT_ID, true);

        when(userRepository.findByIdOptional(USER_ID)).thenReturn(Optional.of(user));
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(alertRepository.findByUserAndMunicipality(USER_ID, MUN_ID))
                .thenReturn(Optional.of(existente));
        when(alertMapper.toResponse(existente)).thenReturn(expected);

        AlertResponse actual = service.subscribe(USER_ID, MUN_ID);

        assertTrue(existente.getIsActive(), "La alerta existente debe reactivarse");
        verify(alertEventProducer).publishAlertCreated(existente);
        // flush() solo se llama al crear una nueva alerta → aquí no debe llamarse
        verify(alertRepository, never()).flush();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("subscribe → sin alerta previa → crea nueva y publica evento")
    void subscribe_sinAlertaPrevia_creaNueva() throws Exception {
        AlertResponse expected = alertResponse(ALERT_ID, true);

        when(userRepository.findByIdOptional(USER_ID)).thenReturn(Optional.of(user));
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(alertRepository.findByUserAndMunicipality(USER_ID, MUN_ID))
                .thenReturn(Optional.empty());
        when(alertMapper.toResponse(any(Alert.class))).thenReturn(expected);

        AlertResponse actual = service.subscribe(USER_ID, MUN_ID);

        verify(alertRepository).flush();
        verify(alertEventProducer).publishAlertCreated(any(Alert.class));
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("subscribe nueva → mensaje incluye nombre del municipio y tipo SUBSCRIPTION")
    void subscribe_nueva_mensajeConNombreMunicipio() {
        when(userRepository.findByIdOptional(USER_ID)).thenReturn(Optional.of(user));
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(alertRepository.findByUserAndMunicipality(USER_ID, MUN_ID))
                .thenReturn(Optional.empty());
        when(alertMapper.toResponse(any())).thenReturn(alertResponse(ALERT_ID, true));

        service.subscribe(USER_ID, MUN_ID);

        // Capturamos desde el event producer (firma unívoca → sin ambigüedad de sobrecargas)
        ArgumentCaptor<Alert> cap = ArgumentCaptor.forClass(Alert.class);
        verify(alertEventProducer).publishAlertCreated(cap.capture());
        assertTrue(cap.getValue().getMessage().contains("Coyoacán"));
        assertEquals("SUBSCRIPTION", cap.getValue().getAlertType());
    }

    // ── activate ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("activate → alerta no encontrada → lanza AppException 404")
    void activate_noEncontrada_lanzaNotFound() {
        when(alertRepository.findByIdOptional(ALERT_ID)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> service.activate(ALERT_ID));

        assertEquals(jakarta.ws.rs.core.Response.Status.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("activate → alerta inactiva → se activa y devuelve response")
    void activate_alertaInactiva_seActiva() throws Exception {
        Alert al = alert(ALERT_ID, user, mun, "INFO", false);
        AlertResponse expected = alertResponse(ALERT_ID, true);

        when(alertRepository.findByIdOptional(ALERT_ID)).thenReturn(Optional.of(al));
        when(alertMapper.toResponse(al)).thenReturn(expected);

        AlertResponse actual = service.activate(ALERT_ID);

        assertTrue(al.getIsActive());
        assertEquals(expected, actual);
    }

    // ── deactivate ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deactivate → alerta no encontrada → lanza AppException 404")
    void deactivate_noEncontrada_lanzaNotFound() {
        when(alertRepository.findByIdOptional(ALERT_ID)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> service.deactivate(ALERT_ID));
    }

    @Test
    @DisplayName("deactivate → alerta activa → se desactiva")
    void deactivate_alertaActiva_seDesactiva() throws Exception {
        Alert al = alert(ALERT_ID, user, mun, "INFO", true);

        when(alertRepository.findByIdOptional(ALERT_ID)).thenReturn(Optional.of(al));

        service.deactivate(ALERT_ID);

        assertFalse(al.getIsActive());
    }

    // ── getByUser / getActiveByUser / getByMunicipality ───────────────────────

    @Test
    @DisplayName("getByUser → devuelve alertas mapeadas del usuario")
    void getByUser_devuelveAlertasMapeadas() throws Exception {
        Alert al = alert(ALERT_ID, user, mun, "INFO", true);
        AlertResponse resp = alertResponse(ALERT_ID, true);

        when(alertRepository.findByUser(USER_ID)).thenReturn(List.of(al));
        when(alertMapper.toResponse(al)).thenReturn(resp);

        List<AlertResponse> result = service.getByUser(USER_ID);

        assertEquals(1, result.size());
        assertEquals(resp, result.get(0));
    }

    @Test
    @DisplayName("getActiveByUser → devuelve solo alertas activas del usuario")
    void getActiveByUser_devuelveSoloActivas() throws Exception {
        Alert al = alert(ALERT_ID, user, mun, "INFO", true);
        AlertResponse resp = alertResponse(ALERT_ID, true);

        when(alertRepository.findActiveByUser(USER_ID)).thenReturn(List.of(al));
        when(alertMapper.toResponse(al)).thenReturn(resp);

        List<AlertResponse> result = service.getActiveByUser(USER_ID);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
    }

    @Test
    @DisplayName("getByMunicipality → devuelve alertas activas del municipio")
    void getByMunicipality_devuelveActivas() throws Exception {
        Alert al = alert(ALERT_ID, user, mun, "RISK_HIGH", true);
        AlertResponse resp = alertResponse(ALERT_ID, true);

        when(alertRepository.findActiveByMunicipality(MUN_ID)).thenReturn(List.of(al));
        when(alertMapper.toResponse(al)).thenReturn(resp);

        List<AlertResponse> result = service.getByMunicipality(MUN_ID);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getByUser → sin alertas → lista vacía")
    void getByUser_sinAlertas_listaVacia() {
        when(alertRepository.findByUser(USER_ID)).thenReturn(List.of());

        List<AlertResponse> result = service.getByUser(USER_ID);

        assertTrue(result.isEmpty());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AlertResponse alertResponse(Long id, boolean active) {
        return new AlertResponse(id,
                new MunicipalitySummary(MUN_ID, "Coyoacán"),
                "INFO", "Mensaje", active, null, Instant.now());
    }

    private Alert alert(Long id, User u, Municipality m, String type, boolean active) throws Exception {
        Alert a = new Alert();
        a.setUser(u);
        a.setMunicipality(m);
        a.setAlertType(type);
        a.setMessage("Mensaje de prueba");
        a.setIsActive(active);
        setField(a, "id", id);
        return a;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
