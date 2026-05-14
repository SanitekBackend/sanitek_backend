package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.request.FiltroMedicionRequest;
import org.acme.dto.response.MedicionResponse;
import org.acme.mapper.MedicionMapper;
import org.acme.repository.MedicionContaminanteRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class MedicionService {

    @Inject MedicionContaminanteRepository medicionRepo;
    @Inject MedicionMapper medicionMapper;

    public List<MedicionResponse> obtenerPorRango(FiltroMedicionRequest filtro) {
        if (filtro.idStation != null && filtro.idContaminante != null) {
            return medicionRepo.findByStationAndContaminante(
                            filtro.idStation, filtro.idContaminante.longValue()).stream()
                    .filter(m -> !m.fecha.isBefore(filtro.desde) && !m.fecha.isAfter(filtro.hasta))
                    .map(medicionMapper::toResponse)
                    .toList();
        }
        if (filtro.idStation != null) {
            return medicionRepo.findByFechaRange(filtro.idStation, filtro.desde, filtro.hasta).stream()
                    .map(medicionMapper::toResponse)
                    .toList();
        }
        // Sin id_station la consulta sería demasiado amplia; retornar vacío obliga al cliente a filtrar
        return List.of();
    }

    public List<MedicionResponse> obtenerUltimaPorStation(String idStation) {
        Instant hasta = Instant.now();
        Instant desde = hasta.minus(7, ChronoUnit.DAYS);
        return medicionRepo.findByFechaRange(idStation, desde, hasta).stream()
                .collect(Collectors.toMap(
                        m -> m.contaminante.id,
                        m -> m,
                        (existing, ignored) -> existing
                ))
                .values().stream()
                .map(medicionMapper::toResponse)
                .toList();
    }
}
