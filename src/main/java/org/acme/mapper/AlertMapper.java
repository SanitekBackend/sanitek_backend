package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Alert;
import org.acme.dto.response.AlertResponse;
import org.acme.dto.response.MunicipalitySummary;

@ApplicationScoped
public class AlertMapper {

    public AlertResponse toResponse(Alert alert) {
        MunicipalitySummary municipality = alert.getMunicipality() != null
                ? new MunicipalitySummary(
                        alert.getMunicipality().getId(),
                        alert.getMunicipality().getMunicipalityName(),
                        alert.getMunicipality().getSocialIndex())
                : null;
        return new AlertResponse(
                alert.getId(),
                municipality,
                alert.getAlertType(),
                alert.getMessage(),
                alert.getIsActive(),
                alert.getScheduledFor(),
                alert.getCreatedAt()
        );
    }
}
