package org.acme.dto.response;

import org.acme.domain.enums.PollutantType;

public record ContaminanteAnualResponse(
    String municipio,
    PollutantType tipoCont,
    Float valorMedicion,
    Integer anio
) {}
