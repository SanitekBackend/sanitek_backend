package org.acme.dto.response;

import java.time.Instant;

public record MedicionResponse(
        Long id,
        String idStation,
        ContaminanteResumen contaminante,
        Instant fecha,
        Float valorMedicion
) {}
