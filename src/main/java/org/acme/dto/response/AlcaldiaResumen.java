package org.acme.dto.response;

import org.acme.domain.enums.NivelRezago;

public record AlcaldiaResumen(Long id, String nombre, NivelRezago nivelRezago) {}
