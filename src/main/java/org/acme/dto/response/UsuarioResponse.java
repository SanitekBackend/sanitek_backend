package org.acme.dto.response;

import java.util.List;

public record UsuarioResponse(
        Long id,
        String firebaseUid,
        String email,
        Boolean activo,
        RolResponse rol,
        List<AlcaldiaResumen> alcaldias
) {}
