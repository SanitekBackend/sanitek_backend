package org.acme.mapper;

import org.acme.domain.entity.Irsa;
import org.acme.domain.entity.Municipality;
import org.acme.dto.response.MunicipalityResponse;
import org.acme.dto.response.MunicipalitySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MunicipalityMapper — conversión entidad → DTO")
class MunicipalityMapperTest {

    private MunicipalityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MunicipalityMapper();
    }

    // ── toResponse ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toResponse con IRSA → todos los campos mapeados correctamente")
    void toResponse_conIrsa_mapeaTodo() throws Exception {
        Municipality mun = municipio(1L, "Iztapalapa", 0.72f);
        Irsa irsa = irsa(55.0f, "MODERATE");

        MunicipalityResponse response = mapper.toResponse(mun, irsa);

        assertEquals(1L,           response.id());
        assertEquals("Iztapalapa", response.municipalityName());
        assertEquals(0.72f,        response.socialVulnerability());

        assertNotNull(response.currentIrsa());
        assertEquals(55.0f,      response.currentIrsa().irsaValue());
        assertEquals("MODERATE", response.currentIrsa().riskLevel());
    }

    @Test
    @DisplayName("toResponse con irsa null → currentIrsa es null")
    void toResponse_sinIrsa_currentIrsaNull() throws Exception {
        Municipality mun = municipio(2L, "Xochimilco", 0.45f);

        MunicipalityResponse response = mapper.toResponse(mun, null);

        assertEquals(2L,         response.id());
        assertEquals("Xochimilco", response.municipalityName());
        assertNull(response.currentIrsa());
    }

    @Test
    @DisplayName("toResponse con socialVulnerability null → se mapea como null")
    void toResponse_vulnerabilidadNull() throws Exception {
        Municipality mun = municipio(3L, "Tláhuac", null);
        Irsa irsa = irsa(30.0f, "LOW");

        MunicipalityResponse response = mapper.toResponse(mun, irsa);

        assertNull(response.socialVulnerability());
        assertEquals("LOW", response.currentIrsa().riskLevel());
    }

    @Test
    @DisplayName("toResponse refleja el createdAt del IRSA en el summary")
    void toResponse_irsaSummaryTieneCreatedAt() throws Exception {
        Municipality mun = municipio(4L, "Gustavo A. Madero", 0.60f);
        Irsa irsaEntity = new Irsa();
        irsaEntity.setIrsaValue(40.0f);
        irsaEntity.setRiskLevel("LOW");
        Instant ts = Instant.parse("2025-04-01T06:00:00Z");
        setField(irsaEntity, "createdAt", ts);

        MunicipalityResponse response = mapper.toResponse(mun, irsaEntity);

        assertEquals(ts, response.currentIrsa().calculatedAt());
    }

    // ── toSummary ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toSummary → mapea id y nombre del municipio")
    void toSummary_mapeaIdYNombre() throws Exception {
        Municipality mun = municipio(5L, "Cuauhtémoc", 0.50f);

        MunicipalitySummary summary = mapper.toSummary(mun);

        assertEquals(5L,          summary.id());
        assertEquals("Cuauhtémoc", summary.municipalityName());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Municipality municipio(Long id, String nombre, Float sv) throws Exception {
        Municipality m = new Municipality();
        m.setMunicipalityName(nombre);
        m.setSocialVulnerability(sv);
        setField(m, "id", id);
        return m;
    }

    private Irsa irsa(float value, String riskLevel) {
        Irsa i = new Irsa();
        i.setIrsaValue(value);
        i.setRiskLevel(riskLevel);
        i.setIsForecast(false);
        return i;
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
