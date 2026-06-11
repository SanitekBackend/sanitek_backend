package org.acme.service;

import org.acme.domain.entity.Irsa;
import org.acme.domain.entity.Municipality;
import org.acme.domain.entity.NO2;
import org.acme.domain.entity.O3;
import org.acme.domain.entity.PM25;
import org.acme.domain.entity.Radiation;
import org.acme.domain.entity.Temperature;
import org.acme.dto.response.IrsaDiagnosticResponse;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.IrsaTrendResponse;
import org.acme.dto.response.MunicipalitySummary;
import org.acme.exception.AppException;
import org.acme.mapper.IrsaMapper;
import org.acme.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IrsaService — lógica de negocio IRSA")
class IrsaServiceTest {

    // ── Repositorios mockeados ────────────────────────────────────────────────
    @Mock IrsaRepository         irsaRepository;
    @Mock MunicipalityRepository municipalityRepository;
    @Mock StationRepository      stationRepository;
    @Mock NO2Repository          no2Repository;
    @Mock O3Repository           o3Repository;
    @Mock PM25Repository         pm25Repository;
    @Mock RadiationRepository    radiationRepository;
    @Mock TemperatureRepository  temperatureRepository;
    @Mock CopdRepository         copdRepository;
    @Mock AsthmaRepository       asthmaRepository;
    @Mock PneumoniaRepository    pneumoniaRepository;
    @Mock SmokingRepository      smokingRepository;
    @Mock IrsaMapper             irsaMapper;

    @InjectMocks IrsaService service;

    // ── Fixtures ──────────────────────────────────────────────────────────────
    private static final Long MUN_ID    = 1L;
    private static final Long IRSA_ID   = 10L;
    private Municipality mun;

    @BeforeEach
    void setUp() throws Exception {
        mun = new Municipality();
        mun.setMunicipalityName("Iztapalapa");
        setField(mun, "id", MUN_ID);
    }

    // ── getLatestByMunicipality ───────────────────────────────────────────────

    @Test
    @DisplayName("getLatestByMunicipality → encontrado → devuelve IrsaResponse")
    void getLatest_encontrado_devuelveResponse() throws Exception {
        Irsa irsa = irsaEntity(IRSA_ID, mun, 45.5f, "MODERATE");
        IrsaResponse expected = irsaResponse(IRSA_ID, "MODERATE");

        when(irsaRepository.findLatestByMunicipality(MUN_ID)).thenReturn(Optional.of(irsa));
        when(irsaMapper.toResponse(irsa)).thenReturn(expected);

        IrsaResponse actual = service.getLatestByMunicipality(MUN_ID);

        assertEquals(expected, actual);
        verify(irsaRepository).findLatestByMunicipality(MUN_ID);
    }

    @Test
    @DisplayName("getLatestByMunicipality → no encontrado → lanza AppException 404")
    void getLatest_noEncontrado_lanzaNotFound() {
        when(irsaRepository.findLatestByMunicipality(MUN_ID)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> service.getLatestByMunicipality(MUN_ID));

        assertEquals(jakarta.ws.rs.core.Response.Status.NOT_FOUND, ex.getStatus());
    }

    // ── listLatestAll ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("listLatestAll → repositorio con 2 registros → devuelve lista de 2")
    void listLatestAll_dosRegistros_devuelveListaDe2() throws Exception {
        Irsa i1 = irsaEntity(1L, mun, 30.0f, "LOW");
        Irsa i2 = irsaEntity(2L, mun, 55.0f, "MODERATE");

        when(irsaRepository.findAllLatest()).thenReturn(List.of(i1, i2));
        when(irsaMapper.toResponse(i1)).thenReturn(irsaResponse(1L, "LOW"));
        when(irsaMapper.toResponse(i2)).thenReturn(irsaResponse(2L, "MODERATE"));

        List<IrsaResponse> result = service.listLatestAll();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("listLatestAll → repositorio vacío → devuelve lista vacía")
    void listLatestAll_vacio_listaVacia() {
        when(irsaRepository.findAllLatest()).thenReturn(List.of());

        List<IrsaResponse> result = service.listLatestAll();

        assertTrue(result.isEmpty());
    }

    // ── getHistorical ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getHistorical → devuelve registros mapeados en el rango")
    void getHistorical_conRegistros_devuelveListaMapeada() throws Exception {
        Instant from = Instant.parse("2025-01-01T00:00:00Z");
        Instant to   = Instant.parse("2025-06-01T00:00:00Z");

        Irsa irsa = irsaEntity(5L, mun, 40.0f, "LOW");
        IrsaResponse resp = irsaResponse(5L, "LOW");

        when(irsaRepository.findHistoricalByMunicipality(MUN_ID, from, to))
                .thenReturn(List.of(irsa));
        when(irsaMapper.toResponse(irsa)).thenReturn(resp);

        List<IrsaResponse> result = service.getHistorical(MUN_ID, from, to);

        assertEquals(1, result.size());
        assertEquals(resp, result.get(0));
    }

    @Test
    @DisplayName("getHistorical → sin registros en el rango → lista vacía")
    void getHistorical_sinRegistros_listaVacia() {
        Instant from = Instant.parse("2020-01-01T00:00:00Z");
        Instant to   = Instant.parse("2020-06-01T00:00:00Z");

        when(irsaRepository.findHistoricalByMunicipality(MUN_ID, from, to))
                .thenReturn(List.of());

        List<IrsaResponse> result = service.getHistorical(MUN_ID, from, to);

        assertTrue(result.isEmpty());
    }

    // ── listByRiskLevel ───────────────────────────────────────────────────────

    @Test
    @DisplayName("listByRiskLevel HIGH → devuelve solo registros HIGH")
    void listByRiskLevel_HIGH_devuelveRegistrosFiltrados() throws Exception {
        Irsa irsa = irsaEntity(7L, mun, 75.0f, "HIGH");
        IrsaResponse resp = irsaResponse(7L, "HIGH");

        when(irsaRepository.findByRiskLevel("HIGH")).thenReturn(List.of(irsa));
        when(irsaMapper.toResponse(irsa)).thenReturn(resp);

        List<IrsaResponse> result = service.listByRiskLevel("HIGH");

        assertEquals(1, result.size());
        assertEquals("HIGH", result.get(0).riskLevel());
    }

    @Test
    @DisplayName("listByRiskLevel nivel inexistente → lista vacía")
    void listByRiskLevel_sinResultados_listaVacia() {
        when(irsaRepository.findByRiskLevel("CRITICAL")).thenReturn(List.of());

        List<IrsaResponse> result = service.listByRiskLevel("CRITICAL");

        assertTrue(result.isEmpty());
    }

    // ── calculate ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("calculate → municipio no encontrado → lanza AppException 404")
    void calculate_municipioNoExiste_lanzaNotFound() {
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> service.calculate(MUN_ID));

        assertEquals(jakarta.ws.rs.core.Response.Status.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("calculate → municipio sin estaciones → lanza AppException 404")
    void calculate_sinEstaciones_lanzaNotFound() {
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(stationRepository.findIdsByMunicipality(MUN_ID)).thenReturn(List.of());

        AppException ex = assertThrows(AppException.class,
                () -> service.calculate(MUN_ID));

        assertEquals(jakarta.ws.rs.core.Response.Status.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("calculate → sin datos de medición → lanza AppException 404")
    void calculate_sinMediciones_lanzaNotFound() {
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(stationRepository.findIdsByMunicipality(MUN_ID)).thenReturn(List.of(100L));

        // Todos los repositorios devuelven Optional.empty → no se puede resolver ventana
        when(no2Repository.findLatestRegisteredAtByStations(anyList())).thenReturn(Optional.empty());
        when(o3Repository.findLatestRegisteredAtByStations(anyList())).thenReturn(Optional.empty());
        when(pm25Repository.findLatestRegisteredAtByStations(anyList())).thenReturn(Optional.empty());
        when(radiationRepository.findLatestRegisteredAtByStations(anyList())).thenReturn(Optional.empty());
        when(temperatureRepository.findLatestRegisteredAtByStations(anyList())).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> service.calculate(MUN_ID));

        assertEquals(jakarta.ws.rs.core.Response.Status.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("calculate → datos válidos → persiste IRSA y devuelve response")
    void calculate_datosValidos_persisteYDevuelveResponse() throws Exception {
        List<Long> stationIds = List.of(100L);
        Instant    latestAt   = Instant.now();
        IrsaResponse expected = irsaResponse(IRSA_ID, "MODERATE");

        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(stationRepository.findIdsByMunicipality(MUN_ID)).thenReturn(stationIds);

        // Ventana de medición
        when(no2Repository.findLatestRegisteredAtByStations(stationIds)).thenReturn(Optional.of(latestAt));
        when(o3Repository.findLatestRegisteredAtByStations(stationIds)).thenReturn(Optional.of(latestAt));
        when(pm25Repository.findLatestRegisteredAtByStations(stationIds)).thenReturn(Optional.of(latestAt));
        when(radiationRepository.findLatestRegisteredAtByStations(stationIds)).thenReturn(Optional.of(latestAt));
        when(temperatureRepository.findLatestRegisteredAtByStations(stationIds)).thenReturn(Optional.of(latestAt));

        // Datos de contaminantes (valores al umbral OMS)
        when(no2Repository.findByStationsAndDateRange(anyList(), any(), any()))
                .thenReturn(List.of(no2("25.0")));
        when(o3Repository.findByStationsAndDateRange(anyList(), any(), any()))
                .thenReturn(List.of(o3("100.0")));
        when(pm25Repository.findByStationsAndDateRange(anyList(), any(), any()))
                .thenReturn(List.of(pm25("15.0")));
        when(radiationRepository.findByStationsAndDateRange(anyList(), any(), any()))
                .thenReturn(List.of(radiation("6.0")));
        when(temperatureRepository.findByStationsAndDateRange(anyList(), any(), any()))
                .thenReturn(List.of(temperature("35.0")));

        // Counts de salud
        when(copdRepository.countByMunicipality(MUN_ID)).thenReturn(10L);
        when(asthmaRepository.countTotalByMunicipality(MUN_ID)).thenReturn(5L);
        when(pneumoniaRepository.countTotalByMunicipality(MUN_ID)).thenReturn(3L);
        when(smokingRepository.countTotalByMunicipality(MUN_ID)).thenReturn(2L);

        when(irsaMapper.toResponse(any(Irsa.class))).thenReturn(expected);

        IrsaResponse actual = service.calculate(MUN_ID);

        assertEquals(expected, actual);
        verify(irsaRepository).persist(any(Irsa.class));
    }

    // ── getTrend ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTrend → municipio no encontrado → lanza AppException 404")
    void getTrend_municipioNoExiste_lanzaNotFound() {
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> service.getTrend(MUN_ID, "WEEKLY", 8));

        assertEquals(jakarta.ws.rs.core.Response.Status.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("getTrend → sin historial → devuelve NO_DATA y lista vacía")
    void getTrend_sinHistorial_noData() {
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(irsaRepository.findHistoricalByMunicipality(eq(MUN_ID), any(), any()))
                .thenReturn(List.of());

        IrsaTrendResponse resp = service.getTrend(MUN_ID, "WEEKLY", 8);

        assertEquals("NO_DATA", resp.trend());
        assertTrue(resp.points().isEmpty());
        assertEquals("WEEKLY", resp.period());
        assertEquals(8, resp.periods());
    }

    @Test
    @DisplayName("getTrend MONTHLY → period en respuesta es MONTHLY")
    void getTrend_monthly_periodEsMonthly() {
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(irsaRepository.findHistoricalByMunicipality(eq(MUN_ID), any(), any()))
                .thenReturn(List.of());

        IrsaTrendResponse resp = service.getTrend(MUN_ID, "monthly", 6);

        assertEquals("MONTHLY", resp.period());
        assertEquals(6, resp.periods());
    }

    @Test
    @DisplayName("getTrend → con historial del mismo periodo → trend STABLE")
    void getTrend_mismoIrsaEnDosRegistros_stable() throws Exception {
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));

        // Dos registros en la misma semana con IRSA similar → variación ≤ 5 → STABLE
        Instant semana = Instant.parse("2025-05-12T10:00:00Z");
        Irsa i1 = irsaConCreatedAt(50.0f, semana);
        Irsa i2 = irsaConCreatedAt(52.0f, semana.plusSeconds(3600));

        when(irsaRepository.findHistoricalByMunicipality(eq(MUN_ID), any(), any()))
                .thenReturn(List.of(i1, i2));

        IrsaTrendResponse resp = service.getTrend(MUN_ID, "WEEKLY", 8);

        assertEquals("STABLE", resp.trend());
    }

    // ── getDailySnapshot ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getDailySnapshot con fecha pasada → llama a findHistoricalByRange")
    void getDailySnapshot_fechaPasada_llamaHistorico() throws Exception {
        LocalDate ayer = LocalDate.now().minusDays(1);

        when(irsaRepository.findHistoricalByRange(any(), any())).thenReturn(List.of());

        List<IrsaResponse> result = service.getDailySnapshot(ayer);

        assertTrue(result.isEmpty());
        verify(irsaRepository).findHistoricalByRange(any(), any());
        verify(irsaRepository, never()).findForecastsByRange(any(), any());
    }

    @Test
    @DisplayName("getDailySnapshot con fecha futura → llama a findForecastsByRange")
    void getDailySnapshot_fechaFutura_llamaForecasts() {
        LocalDate manana = LocalDate.now().plusDays(1);

        when(irsaRepository.findForecastsByRange(any(), any())).thenReturn(List.of());

        List<IrsaResponse> result = service.getDailySnapshot(manana);

        assertTrue(result.isEmpty());
        verify(irsaRepository).findForecastsByRange(any(), any());
        verify(irsaRepository, never()).findHistoricalByRange(any(), any());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Irsa irsaEntity(Long id, Municipality m, float value, String level) throws Exception {
        Irsa i = new Irsa();
        i.setMunicipality(m);
        i.setIrsaValue(value);
        i.setRiskLevel(level);
        i.setIsForecast(false);
        setField(i, "id", id);
        setField(i, "createdAt", Instant.now());
        return i;
    }

    private Irsa irsaConCreatedAt(float value, Instant createdAt) throws Exception {
        Irsa i = new Irsa();
        i.setMunicipality(mun);
        i.setIrsaValue(value);
        i.setRiskLevel("MODERATE");
        i.setIsForecast(false);
        setField(i, "createdAt", createdAt);
        return i;
    }

    private IrsaResponse irsaResponse(Long id, String level) {
        return new IrsaResponse(id,
                new MunicipalitySummary(MUN_ID, "Iztapalapa"),
                45.5f, level, false, null, Instant.now());
    }

    private NO2 no2(String val) {
        NO2 n = new NO2();
        n.setMetricValue(val);
        return n;
    }

    private O3 o3(String val) {
        O3 o = new O3();
        o.setMetricValue(val);
        return o;
    }

    private PM25 pm25(String val) {
        PM25 p = new PM25();
        p.setMetricValue(val);
        return p;
    }

    private Radiation radiation(String val) {
        Radiation r = new Radiation();
        r.setMetricValue(val);
        return r;
    }

    private Temperature temperature(String val) {
        Temperature t = new Temperature();
        t.setMetricValue(val);
        return t;
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
