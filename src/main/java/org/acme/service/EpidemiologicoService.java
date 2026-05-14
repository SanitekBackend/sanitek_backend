package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.response.EpidemiologicoResponse;
import org.acme.mapper.EpidemiologicoMapper;
import org.acme.repository.EpidemiologicoRepository;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class EpidemiologicoService {

    @Inject EpidemiologicoRepository epiRepo;
    @Inject EpidemiologicoMapper epiMapper;

    public List<EpidemiologicoResponse> obtenerPorAlcaldia(Long idAlcaldia) {
        return epiRepo.findByAlcaldia(idAlcaldia).stream()
                .map(epiMapper::toResponse)
                .toList();
    }

    public List<EpidemiologicoResponse> obtenerPorAlcaldiaYFecha(Long idAlcaldia, LocalDate fecha) {
        return epiRepo.findByAlcaldiaAndFecha(idAlcaldia, fecha).stream()
                .map(epiMapper::toResponse)
                .toList();
    }
}
