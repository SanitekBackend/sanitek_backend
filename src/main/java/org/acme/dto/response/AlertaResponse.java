package org.acme.dto.response;

import org.acme.domain.enums.EstadoAlerta;
import java.time.Instant;

public record AlertaResponse(
        Long id,
        AlcaldiaResumen alcaldia,
        String tipoAlerta,
        String mensaje,
        EstadoAlerta estado,
        Instant fechaAlerta
) {}
