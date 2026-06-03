package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Irsa;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.IrsaSummary;
import org.acme.dto.response.MunicipalitySummary;

@ApplicationScoped
public class IrsaMapper {

    public IrsaResponse toResponse(Irsa irsa) {
        MunicipalitySummary municipality = irsa.getMunicipality() != null
                ? new MunicipalitySummary(
                        irsa.getMunicipality().getId(),
                        irsa.getMunicipality().getMunicipalityName())
                : null;
        return new IrsaResponse(
                irsa.getId(),
                municipality,
                irsa.getIrsaValue(),
                irsa.getRiskLevel(),
                irsa.getIsForecast(),
                irsa.getForecastDate(),
                irsa.getCreatedAt()
        );
    }

    public IrsaSummary toSummary(Irsa irsa) {
        return new IrsaSummary(irsa.getIrsaValue(), irsa.getRiskLevel(), irsa.getCreatedAt());
    }
}
