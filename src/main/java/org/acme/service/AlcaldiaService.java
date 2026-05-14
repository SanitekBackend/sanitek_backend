package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.entity.Alcaldia;
import org.acme.domain.entity.Irsa;
import org.acme.domain.enums.NivelRezago;
import org.acme.dto.response.AlcaldiaResponse;
import org.acme.exception.AppException;
import org.acme.mapper.AlcaldiaMapper;
import org.acme.repository.AlcaldiaRepository;
import org.acme.repository.IrsaRepository;
import java.util.List;

@ApplicationScoped
public class AlcaldiaService {

    @Inject AlcaldiaRepository alcaldiaRepo;
    @Inject IrsaRepository irsaRepo;
    @Inject AlcaldiaMapper alcaldiaMapper;

    // Cada alcaldía genera una query IRSA adicional (N+1 intencional);
    // optimizable con JOIN FETCH si el volumen de alcaldías crece
    public List<AlcaldiaResponse> obtenerTodas() {
        return alcaldiaRepo.listAll().stream()
                .map(a -> {
                    Irsa irsa = irsaRepo.findLatestByAlcaldia(a.id).orElse(null);
                    return alcaldiaMapper.toResponse(a, irsa);
                })
                .toList();
    }

    public AlcaldiaResponse obtenerPorId(Long id) {
        Alcaldia a = alcaldiaRepo.findByIdOptional(id)
                .orElseThrow(() -> AppException.notFound("Alcaldía no encontrada"));
        Irsa irsa = irsaRepo.findLatestByAlcaldia(id).orElse(null);
        return alcaldiaMapper.toResponse(a, irsa);
    }

    public List<AlcaldiaResponse> filtrarPorNivelRezago(NivelRezago nivel) {
        return alcaldiaRepo.findByNivelRezago(nivel).stream()
                .map(a -> {
                    Irsa irsa = irsaRepo.findLatestByAlcaldia(a.id).orElse(null);
                    return alcaldiaMapper.toResponse(a, irsa);
                })
                .toList();
    }
}
