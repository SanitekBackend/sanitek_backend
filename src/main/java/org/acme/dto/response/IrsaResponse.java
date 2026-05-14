package org.acme.dto.response;

import org.acme.domain.enums.NivelRiesgo;
import java.time.Instant;

public record IrsaResponse(
        Long id,
        AlcaldiaResumen alcaldia,
        Float valorIrsa,
        double irsa,               // valorIrsa × 100, escala 0-100 (mayor = más crítico)
        NivelRiesgo nivelRiesgo,
        Instant fechaCalculo,
        Float prediccionFutura,
        Instant fechaPrediccion,
        String origenCalculo
) {}
