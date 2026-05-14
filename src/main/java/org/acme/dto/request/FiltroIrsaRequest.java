package org.acme.dto.request;

import jakarta.ws.rs.QueryParam;
import org.acme.domain.enums.NivelRiesgo;
import java.time.Instant;

public class FiltroIrsaRequest {

    @QueryParam("id_alcaldia")
    public Long idAlcaldia;

    @QueryParam("nivel_riesgo")
    public NivelRiesgo nivelRiesgo;

    @QueryParam("desde")
    public Instant desde;

    @QueryParam("hasta")
    public Instant hasta;
}
