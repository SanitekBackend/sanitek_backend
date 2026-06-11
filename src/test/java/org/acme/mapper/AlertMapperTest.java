package org.acme.mapper;

import org.acme.domain.entity.Alert;
import org.acme.domain.entity.Municipality;
import org.acme.domain.entity.User;
import org.acme.dto.response.AlertResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AlertMapper — conversión entidad → DTO")
class AlertMapperTest {

    private AlertMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AlertMapper();
    }

    @Test
    @DisplayName("toResponse con municipio → todos los campos mapeados")
    void toResponse_conMunicipio_mapeaTodo() throws Exception {
        Municipality mun  = municipio(3L, "Coyoacán");
        User         user = usuario(1L);
        Alert        al   = alerta(10L, user, mun, "RISK_HIGH", "Alerta de riesgo", true, null);
        Instant      ts   = Instant.parse("2025-05-01T12:00:00Z");
        setField(al, "createdAt", ts);

        AlertResponse resp = mapper.toResponse(al);

        assertEquals(10L,          resp.id());
        assertEquals("RISK_HIGH",  resp.alertType());
        assertEquals("Alerta de riesgo", resp.message());
        assertTrue(resp.isActive());
        assertNull(resp.scheduledFor());
        assertEquals(ts,           resp.createdAt());

        assertNotNull(resp.municipality());
        assertEquals(3L,         resp.municipality().id());
        assertEquals("Coyoacán", resp.municipality().municipalityName());
    }

    @Test
    @DisplayName("toResponse con municipio null → municipality en response es null")
    void toResponse_municipioNull_esNull() throws Exception {
        User  user = usuario(2L);
        Alert al   = alerta(5L, user, null, "SUBSCRIPTION", "Suscripción", true, null);

        AlertResponse resp = mapper.toResponse(al);

        assertNull(resp.municipality());
        assertEquals("SUBSCRIPTION", resp.alertType());
    }

    @Test
    @DisplayName("toResponse con isActive=false → se refleja en el DTO")
    void toResponse_inactiva_isActiveFalse() throws Exception {
        Municipality mun = municipio(1L, "Álvaro Obregón");
        User         user = usuario(3L);
        Alert        al   = alerta(7L, user, mun, "INFO", "Mensaje", false, null);

        AlertResponse resp = mapper.toResponse(al);

        assertFalse(resp.isActive());
    }

    @Test
    @DisplayName("toResponse con scheduledFor → se mapea la fecha programada")
    void toResponse_conScheduledFor() throws Exception {
        Municipality mun      = municipio(2L, "Tlalpan");
        User         user     = usuario(4L);
        Instant      scheduled = Instant.parse("2025-06-15T08:00:00Z");
        Alert        al        = alerta(8L, user, mun, "SCHEDULED", "Programada", true, scheduled);

        AlertResponse resp = mapper.toResponse(al);

        assertEquals(scheduled, resp.scheduledFor());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Municipality municipio(Long id, String nombre) throws Exception {
        Municipality m = new Municipality();
        m.setMunicipalityName(nombre);
        setField(m, "id", id);
        return m;
    }

    private User usuario(Long id) throws Exception {
        User u = new User();
        setField(u, "id", id);
        return u;
    }

    private Alert alerta(Long id, User user, Municipality mun,
                         String type, String msg, boolean active, Instant scheduled) throws Exception {
        Alert a = new Alert();
        a.setUser(user);
        a.setMunicipality(mun);
        a.setAlertType(type);
        a.setMessage(msg);
        a.setIsActive(active);
        a.setScheduledFor(scheduled);
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
