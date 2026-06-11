package org.acme.mapper;

import org.acme.domain.entity.Irsa;
import org.acme.domain.entity.Municipality;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.IrsaSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IrsaMapper — conversión entidad → DTO")
class IrsaMapperTest {

    private IrsaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new IrsaMapper();
    }

    // ── toResponse ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toResponse con municipio → todos los campos mapeados correctamente")
    void toResponse_conMunicipio_mapeaCampos() throws Exception {
        Municipality mun = municipio(1L, "Álvaro Obregón");
        Irsa irsa = irsa(10L, mun, 45.5f, "MODERATE", false, null);
        Instant ahora = setCreatedAt(irsa, Instant.parse("2025-01-15T10:00:00Z"));

        IrsaResponse response = mapper.toResponse(irsa);

        assertEquals(10L,        response.id());
        assertEquals(45.5f,      response.irsaValue());
        assertEquals("MODERATE", response.riskLevel());
        assertFalse(response.isForecast());
        assertNull(response.forecastDate());
        assertEquals(ahora,      response.calculatedAt());

        assertNotNull(response.municipality());
        assertEquals(1L,                  response.municipality().id());
        assertEquals("Álvaro Obregón",    response.municipality().municipalityName());
    }

    @Test
    @DisplayName("toResponse con municipio null → municipality en response es null")
    void toResponse_municipioNull_municipalityEsNull() {
        Irsa irsa = irsa(5L, null, 30.0f, "LOW", false, null);

        IrsaResponse response = mapper.toResponse(irsa);

        assertNull(response.municipality());
        assertEquals(30.0f, response.irsaValue());
        assertEquals("LOW", response.riskLevel());
    }

    @Test
    @DisplayName("toResponse con isForecast=true y forecastDate → se mapean correctamente")
    void toResponse_forecastConFecha() throws Exception {
        Municipality mun = municipio(2L, "Benito Juárez");
        Instant fechaPronostico = Instant.parse("2025-02-01T00:00:00Z");
        Irsa irsa = irsa(20L, mun, 60.0f, "MODERATE", true, fechaPronostico);

        IrsaResponse response = mapper.toResponse(irsa);

        assertTrue(response.isForecast());
        assertEquals(fechaPronostico, response.forecastDate());
    }

    @Test
    @DisplayName("toResponse con riskLevel HIGH → se refleja en el DTO")
    void toResponse_riskLevelHigh() {
        Irsa irsa = irsa(3L, null, 75.0f, "HIGH", false, null);

        IrsaResponse response = mapper.toResponse(irsa);

        assertEquals("HIGH", response.riskLevel());
        assertEquals(75.0f,  response.irsaValue());
    }

    // ── toSummary ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toSummary → mapea irsaValue, riskLevel y createdAt")
    void toSummary_mapeaCamposBasicos() throws Exception {
        Municipality mun = municipio(1L, "Coyoacán");
        Irsa irsa = irsa(7L, mun, 35.0f, "LOW", false, null);
        Instant ts = setCreatedAt(irsa, Instant.parse("2025-03-10T08:00:00Z"));

        IrsaSummary summary = mapper.toSummary(irsa);

        assertEquals(35.0f,  summary.irsaValue());
        assertEquals("LOW",  summary.riskLevel());
        assertEquals(ts,     summary.calculatedAt());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Municipality municipio(Long id, String nombre) throws Exception {
        Municipality m = new Municipality();
        m.setMunicipalityName(nombre);
        setField(m, "id", id);
        return m;
    }

    private Irsa irsa(Long id, Municipality mun, float value, String riskLevel,
                      boolean isForecast, Instant forecastDate) {
        Irsa i = new Irsa();
        i.setMunicipality(mun);
        i.setIrsaValue(value);
        i.setRiskLevel(riskLevel);
        i.setIsForecast(isForecast);
        i.setForecastDate(forecastDate);
        try { setField(i, "id", id); } catch (Exception e) { throw new RuntimeException(e); }
        return i;
    }

    /** Asigna createdAt por reflexión (campo de BaseEntity) y lo devuelve. */
    private Instant setCreatedAt(Object entity, Instant value) throws Exception {
        Field f = entity.getClass().getSuperclass().getDeclaredField("createdAt");
        f.setAccessible(true);
        f.set(entity, value);
        return value;
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
