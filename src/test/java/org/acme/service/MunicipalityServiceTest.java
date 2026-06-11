package org.acme.service;

import org.acme.domain.entity.Irsa;
import org.acme.domain.entity.Municipality;
import org.acme.dto.response.IrsaSummary;
import org.acme.dto.response.MunicipalityResponse;
import org.acme.exception.AppException;
import org.acme.mapper.MunicipalityMapper;
import org.acme.repository.IrsaRepository;
import org.acme.repository.MunicipalityRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MunicipalityService — gestión de municipalidades")
class MunicipalityServiceTest {

    @Mock MunicipalityRepository municipalityRepository;
    @Mock IrsaRepository         irsaRepository;
    @Mock MunicipalityMapper     municipalityMapper;

    @InjectMocks MunicipalityService service;

    private static final Long MUN_ID = 1L;
    private Municipality mun;

    @BeforeEach
    void setUp() throws Exception {
        mun = new Municipality();
        mun.setMunicipalityName("Álvaro Obregón");
        mun.setSocialVulnerability(0.65f);
        setField(mun, "id", MUN_ID);
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAll → repositorio con 2 municipalidades → devuelve lista de 2")
    void getAll_dosMunicipalidades_devuelveListaDe2() throws Exception {
        Municipality mun2 = new Municipality();
        mun2.setMunicipalityName("Benito Juárez");
        setField(mun2, "id", 2L);

        Irsa irsa1 = irsaEntity(10L, 40.0f, "LOW");
        MunicipalityResponse resp1 = munResponse(MUN_ID, "Álvaro Obregón", 40.0f, "LOW");
        MunicipalityResponse resp2 = munResponse(2L,     "Benito Juárez",  null,  null);

        when(municipalityRepository.listAll()).thenReturn(List.of(mun, mun2));
        when(irsaRepository.findLatestByMunicipality(MUN_ID)).thenReturn(Optional.of(irsa1));
        when(irsaRepository.findLatestByMunicipality(2L)).thenReturn(Optional.empty());
        when(municipalityMapper.toResponse(mun,  irsa1)).thenReturn(resp1);
        when(municipalityMapper.toResponse(mun2, null)).thenReturn(resp2);

        List<MunicipalityResponse> result = service.getAll();

        assertEquals(2, result.size());
        verify(municipalityRepository).listAll();
        verify(irsaRepository, times(2)).findLatestByMunicipality(anyLong());
    }

    @Test
    @DisplayName("getAll → municipalidad sin IRSA → se pasa null al mapper")
    void getAll_sinIrsa_pasaNullAlMapper() {
        when(municipalityRepository.listAll()).thenReturn(List.of(mun));
        when(irsaRepository.findLatestByMunicipality(MUN_ID)).thenReturn(Optional.empty());
        when(municipalityMapper.toResponse(mun, null))
                .thenReturn(munResponse(MUN_ID, "Álvaro Obregón", null, null));

        List<MunicipalityResponse> result = service.getAll();

        assertEquals(1, result.size());
        verify(municipalityMapper).toResponse(mun, null);
    }

    @Test
    @DisplayName("getAll → repositorio vacío → lista vacía")
    void getAll_repositorioVacio_listaVacia() {
        when(municipalityRepository.listAll()).thenReturn(List.of());

        List<MunicipalityResponse> result = service.getAll();

        assertTrue(result.isEmpty());
        verify(irsaRepository, never()).findLatestByMunicipality(anyLong());
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById → encontrado con IRSA → devuelve response con currentIrsa")
    void getById_encontradoConIrsa_devuelveResponse() throws Exception {
        Irsa irsa = irsaEntity(5L, 55.0f, "MODERATE");
        MunicipalityResponse expected = munResponse(MUN_ID, "Álvaro Obregón", 55.0f, "MODERATE");

        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(irsaRepository.findLatestByMunicipality(MUN_ID)).thenReturn(Optional.of(irsa));
        when(municipalityMapper.toResponse(mun, irsa)).thenReturn(expected);

        MunicipalityResponse actual = service.getById(MUN_ID);

        assertEquals(expected, actual);
        assertEquals("MODERATE", actual.currentIrsa().riskLevel());
    }

    @Test
    @DisplayName("getById → encontrado sin IRSA → currentIrsa es null")
    void getById_encontradoSinIrsa_currentIrsaNull() {
        MunicipalityResponse expected = munResponse(MUN_ID, "Álvaro Obregón", null, null);

        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(irsaRepository.findLatestByMunicipality(MUN_ID)).thenReturn(Optional.empty());
        when(municipalityMapper.toResponse(mun, null)).thenReturn(expected);

        MunicipalityResponse actual = service.getById(MUN_ID);

        assertNull(actual.currentIrsa());
    }

    @Test
    @DisplayName("getById → no encontrado → lanza AppException 404")
    void getById_noEncontrado_lanzaNotFound() {
        when(municipalityRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> service.getById(999L));

        assertEquals(jakarta.ws.rs.core.Response.Status.NOT_FOUND, ex.getStatus());
        verify(irsaRepository, never()).findLatestByMunicipality(anyLong());
    }

    @Test
    @DisplayName("getById → consulta IRSA con el mismo id del municipio")
    void getById_consultaIrsaConElMismoId() throws Exception {
        Irsa irsa = irsaEntity(3L, 30.0f, "LOW");
        when(municipalityRepository.findByIdOptional(MUN_ID)).thenReturn(Optional.of(mun));
        when(irsaRepository.findLatestByMunicipality(MUN_ID)).thenReturn(Optional.of(irsa));
        when(municipalityMapper.toResponse(any(), any())).thenReturn(
                munResponse(MUN_ID, "Álvaro Obregón", 30.0f, "LOW"));

        service.getById(MUN_ID);

        verify(irsaRepository).findLatestByMunicipality(MUN_ID);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MunicipalityResponse munResponse(Long id, String name, Float irsa, String level) {
        IrsaSummary summary = (irsa != null)
                ? new IrsaSummary(irsa, level, Instant.now())
                : null;
        return new MunicipalityResponse(id, name, 0.65f, summary);
    }

    private Irsa irsaEntity(Long id, float value, String level) throws Exception {
        Irsa i = new Irsa();
        i.setIrsaValue(value);
        i.setRiskLevel(level);
        i.setIsForecast(false);
        setField(i, "id", id);
        setField(i, "createdAt", Instant.now());
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
