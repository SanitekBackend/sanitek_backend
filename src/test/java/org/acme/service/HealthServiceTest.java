package org.acme.service;

import org.acme.domain.entity.Municipality;
import org.acme.dto.response.HealthSummaryResponse;
import org.acme.exception.AppException;
import org.acme.repository.AsthmaRepository;
import org.acme.repository.CopdRepository;
import org.acme.repository.MunicipalityRepository;
import org.acme.repository.PneumoniaRepository;
import org.acme.repository.SmokingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("HealthService — resumen de salud poblacional")
class HealthServiceTest {

    @Mock AsthmaRepository       asthmaRepository;
    @Mock CopdRepository         copdRepository;
    @Mock PneumoniaRepository    pneumoniaRepository;
    @Mock SmokingRepository      smokingRepository;
    @Mock MunicipalityRepository municipalityRepository;

    @InjectMocks HealthService service;

    private static final Long MUN_ID = 1L;
    private Municipality mun;

    @BeforeEach
    void setUp() throws Exception {
        mun = new Municipality();
        mun.setMunicipalityName("Iztacalco");
        setField(mun, "id", MUN_ID);
    }

    // ── calculateHealthScore ──────────────────────────────────────────────────

    @Test
    @DisplayName("calculateHealthScore → sin casos → devuelve 50.0 (neutral)")
    void healthScore_sinCasos_devuelve50() {
        when(asthmaRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(0L);
        when(copdRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(0L);
        when(pneumoniaRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(0L);
        when(smokingRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(0L);

        double score = service.calculateHealthScore(MUN_ID);

        assertEquals(50.0, score, 0.001);
    }

    @Test
    @DisplayName("calculateHealthScore → 100 casos → score = 80.0 (100 - 100/5)")
    void healthScore_100Casos_devuelve80() {
        // total = 25+25+25+25 = 100 → 100 - 100/5 = 80
        when(asthmaRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(25L);
        when(copdRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(25L);
        when(pneumoniaRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(25L);
        when(smokingRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(25L);

        double score = service.calculateHealthScore(MUN_ID);

        assertEquals(80.0, score, 0.001);
    }

    @Test
    @DisplayName("calculateHealthScore → 500 casos → score = 0.0 (límite inferior)")
    void healthScore_500Casos_devuelve0() {
        // total = 500 → 100 - 500/5 = 100 - 100 = 0
        when(asthmaRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(125L);
        when(copdRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(125L);
        when(pneumoniaRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(125L);
        when(smokingRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(125L);

        double score = service.calculateHealthScore(MUN_ID);

        assertEquals(0.0, score, 0.001);
    }

    @Test
    @DisplayName("calculateHealthScore → más de 500 casos → score no baja de 0")
    void healthScore_masDe500Casos_clampedA0() {
        // total = 600 → max(0, 100 - 120) = 0
        when(asthmaRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(150L);
        when(copdRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(150L);
        when(pneumoniaRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(150L);
        when(smokingRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(150L);

        double score = service.calculateHealthScore(MUN_ID);

        assertEquals(0.0, score, 0.001);
        assertTrue(score >= 0.0, "El score de salud no puede ser negativo");
    }

    @Test
    @DisplayName("calculateHealthScore → solo casos de asma → score calculado correctamente")
    void healthScore_soloCasosAsma_calculaCorrecto() {
        // total = 50 → 100 - 50/5 = 90
        when(asthmaRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(50L);
        when(copdRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(0L);
        when(pneumoniaRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(0L);
        when(smokingRepository.countByMunicipalityAndSince(eq(MUN_ID), any(Instant.class))).thenReturn(0L);

        double score = service.calculateHealthScore(MUN_ID);

        assertEquals(90.0, score, 0.001);
    }

    // ── getSummaryByMunicipality ──────────────────────────────────────────────

    @Test
    @DisplayName("getSummaryByMunicipality → municipio no encontrado → lanza AppException 404")
    void getSummary_municipioNoExiste_lanzaNotFound() {
        when(municipalityRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> service.getSummaryByMunicipality(999L));

        assertEquals(jakarta.ws.rs.core.Response.Status.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("getSummaryByMunicipality → encontrado → devuelve todos los conteos")
    void getSummary_encontrado_devuelveConteos() {
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(asthmaRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(10L);
        when(copdRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(5L);
        when(pneumoniaRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(8L);
        when(smokingRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(3L);

        HealthSummaryResponse resp = service.getSummaryByMunicipality(MUN_ID);

        assertEquals(MUN_ID,       resp.municipalityId());
        assertEquals("Iztacalco",  resp.municipalityName());
        assertEquals(10L,          resp.asthmaCount());
        assertEquals(5L,           resp.copdCount());
        assertEquals(8L,           resp.pneumoniaCount());
        assertEquals(3L,           resp.smokingCount());
        assertEquals(26L,          resp.totalCases());   // 10+5+8+3
    }

    @Test
    @DisplayName("getSummaryByMunicipality → totalCases = suma de los 4 conteos")
    void getSummary_totalCasesEsSuma() {
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(asthmaRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(20L);
        when(copdRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(30L);
        when(pneumoniaRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(15L);
        when(smokingRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(5L);

        HealthSummaryResponse resp = service.getSummaryByMunicipality(MUN_ID);

        assertEquals(70L, resp.totalCases());
    }

    @Test
    @DisplayName("getSummaryByMunicipality → sin casos → totalCases = 0")
    void getSummary_sinCasos_totalCerosCero() {
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(asthmaRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(0L);
        when(copdRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(0L);
        when(pneumoniaRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(0L);
        when(smokingRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(0L);

        HealthSummaryResponse resp = service.getSummaryByMunicipality(MUN_ID);

        assertEquals(0L, resp.totalCases());
    }

    // ── getAllSummaries ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllSummaries → 2 municipalidades → llama getSummaryByMunicipality para cada una")
    void getAllSummaries_dosMunicipalidades_llamaPorCada() throws Exception {
        Municipality mun2 = new Municipality();
        mun2.setMunicipalityName("Venustiano Carranza");
        setField(mun2, "id", 2L);

        when(municipalityRepository.listAll()).thenReturn(List.of(mun, mun2));

        // mun1
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(asthmaRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(5L);
        when(copdRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(3L);
        when(pneumoniaRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(2L);
        when(smokingRepository.countByMunicipalityAndSince(eq(MUN_ID), eq(Instant.EPOCH))).thenReturn(1L);

        // mun2
        when(municipalityRepository.findByIdOptional(2L)).thenReturn(Optional.of(mun2));
        when(asthmaRepository.countByMunicipalityAndSince(eq(2L), eq(Instant.EPOCH))).thenReturn(0L);
        when(copdRepository.countByMunicipalityAndSince(eq(2L), eq(Instant.EPOCH))).thenReturn(0L);
        when(pneumoniaRepository.countByMunicipalityAndSince(eq(2L), eq(Instant.EPOCH))).thenReturn(0L);
        when(smokingRepository.countByMunicipalityAndSince(eq(2L), eq(Instant.EPOCH))).thenReturn(0L);

        List<HealthSummaryResponse> results = service.getAllSummaries();

        assertEquals(2, results.size());
        verify(municipalityRepository, times(2)).findByIdOptional(anyLong());
    }

    @Test
    @DisplayName("getAllSummaries → repositorio vacío → lista vacía")
    void getAllSummaries_repositorioVacio_listaVacia() {
        when(municipalityRepository.listAll()).thenReturn(List.of());

        List<HealthSummaryResponse> results = service.getAllSummaries();

        assertTrue(results.isEmpty());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
