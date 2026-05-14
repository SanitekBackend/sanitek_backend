package org.acme.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

// Tabla pivote M:M entre Usuario y Alcaldia — no extiende BaseEntity (clave compuesta)
@Entity
@Table(name = "usuarios_alcaldias")
public class UsuarioAlcaldia {

    @EmbeddedId
    public UsuarioAlcaldiaId id;

    // @JsonIgnore evita recursión infinita si se serializa directamente
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario")
    @JoinColumn(name = "id_usuario")
    @JsonIgnore
    public Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idAlcaldia")
    @JoinColumn(name = "id_alcaldia")
    @JsonIgnore
    public Alcaldia alcaldia;

    @Column(name = "fecha_suscripcion", nullable = false)
    public Instant fechaSuscripcion = Instant.now();
}
