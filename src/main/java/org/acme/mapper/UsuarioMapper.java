package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Usuario;
import org.acme.dto.response.AlcaldiaResumen;
import org.acme.dto.response.RolResponse;
import org.acme.dto.response.UsuarioResponse;
import java.util.List;

@ApplicationScoped
public class UsuarioMapper {

    // alcaldias se recibe por parámetro porque Usuario no tiene @OneToMany a UsuarioAlcaldia;
    // el servicio las carga desde UsuarioAlcaldiaRepository y las convierte antes de llamar aquí
    public UsuarioResponse toResponse(Usuario u, List<AlcaldiaResumen> alcaldias) {
        RolResponse rolResponse = u.rol != null
                ? new RolResponse(u.rol.id, u.rol.nombreRol)
                : null;
        return new UsuarioResponse(u.id, u.firebaseUid, u.email, u.activo, rolResponse, alcaldias);
    }
}
