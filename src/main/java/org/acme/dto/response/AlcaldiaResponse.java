package org.acme.dto.response;

import org.acme.domain.enums.NivelRezago;

public record AlcaldiaResponse(
        Long id,
        String nombre,
        Float indiceRezagoSocial,
        NivelRezago nivelRezago,
        IrsaResumen irsaActual   // null si aún no hay cálculo registrado
) {}
