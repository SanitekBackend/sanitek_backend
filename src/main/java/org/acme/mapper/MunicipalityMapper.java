package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Irsa;
import org.acme.domain.entity.Municipality;
import org.acme.dto.response.IrsaSummary;
import org.acme.dto.response.MunicipalityResponse;
import org.acme.dto.response.MunicipalitySummary;

@ApplicationScoped
public class MunicipalityMapper {

    public MunicipalityResponse toResponse(Municipality m, Irsa irsa) {
        IrsaSummary irsaSummary = irsa != null
                ? new IrsaSummary(irsa.getIrsaValue(), irsa.getRiskLevel(), irsa.getCreatedAt())
                : null;
        return new MunicipalityResponse(
                m.getId(),
                m.getMunicipalityName(),
                m.getSocialVulnerability(),
                m.getSocialIndex(),
                irsaSummary
        );
    }

    public MunicipalitySummary toSummary(Municipality m) {
        return new MunicipalitySummary(m.getId(), m.getMunicipalityName(), m.getSocialIndex());
    }
}
