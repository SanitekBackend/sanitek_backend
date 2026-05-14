package org.acme.dto.response;

public record EstacionResponse(
        String idStation,
        String nombre,
        Float latitud,
        Float longitud,
        AlcaldiaResumen alcaldia
) {}
