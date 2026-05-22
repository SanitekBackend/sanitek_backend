package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.entity.Irsa;
import org.acme.domain.entity.Municipality;
import org.acme.dto.response.MunicipalityResponse;
import org.acme.exception.AppException;
import org.acme.mapper.MunicipalityMapper;
import org.acme.repository.IrsaRepository;
import org.acme.repository.MunicipalityRepository;

import java.util.List;

@ApplicationScoped
public class MunicipalityService {

    @Inject MunicipalityRepository municipalityRepository;
    @Inject IrsaRepository irsaRepository;
    @Inject MunicipalityMapper municipalityMapper;

    public List<MunicipalityResponse> getAll() {
        return municipalityRepository.listAll().stream()
                .map(m -> {
                    Irsa irsa = irsaRepository.findLatestByMunicipality(m.getId()).orElse(null);
                    return municipalityMapper.toResponse(m, irsa);
                })
                .toList();
    }

    public List<MunicipalityResponse> getBySocialIndex(String socialIndex) {
        return municipalityRepository.findBySocialIndex(socialIndex).stream()
                .map(m -> {
                    Irsa irsa = irsaRepository.findLatestByMunicipality(m.getId()).orElse(null);
                    return municipalityMapper.toResponse(m, irsa);
                })
                .toList();
    }

    public MunicipalityResponse getById(Long id) {
        Municipality m = municipalityRepository.findByIdOptional(id)
                .orElseThrow(() -> AppException.notFound("Municipality not found"));
        Irsa irsa = irsaRepository.findLatestByMunicipality(id).orElse(null);
        return municipalityMapper.toResponse(m, irsa);
    }
}
