package org.acme.dto.response;

import org.acme.domain.enums.NivelRiesgo;
import java.time.Instant;

public record IrsaResumen(Float valorIrsa, NivelRiesgo nivelRiesgo, Instant fechaCalculo) {}
