package org.acme.dto.response;

import java.time.Instant;

public record DatoMeteorologicoResponse(
        Long id,
        String claveEstacion,
        Instant fecha,
        Float humedadRelativa,
        Float temperaturaAmbiental,
        Float direccionViento,
        Float velocidadViento
) {}
