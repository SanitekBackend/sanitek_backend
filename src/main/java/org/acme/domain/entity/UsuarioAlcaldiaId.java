package org.acme.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UsuarioAlcaldiaId implements Serializable {

    @Column(name = "id_usuario")
    public Long idUsuario;

    @Column(name = "id_alcaldia")
    public Long idAlcaldia;

    public UsuarioAlcaldiaId() {}

    public UsuarioAlcaldiaId(Long idUsuario, Long idAlcaldia) {
        this.idUsuario = idUsuario;
        this.idAlcaldia = idAlcaldia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioAlcaldiaId that)) return false;
        return Objects.equals(idUsuario, that.idUsuario) && Objects.equals(idAlcaldia, that.idAlcaldia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, idAlcaldia);
    }
}
