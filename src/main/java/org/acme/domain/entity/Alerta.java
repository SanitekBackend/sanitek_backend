package org.acme.domain.entity;

import jakarta.persistence.*;
import org.acme.domain.enums.EstadoAlerta;
import java.time.Instant;

@Entity
@Table(name = "alerta",
        indexes = @Index(name = "idx_alerta_usuario_estado", columnList = "id_usuario, estado"))
@AttributeOverride(name = "id", column = @Column(name = "id_alerta"))
public class Alerta extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    public Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alcaldia", nullable = false)
    public Alcaldia alcaldia;

    @Column(name = "tipo_alerta", length = 100)
    public String tipoAlerta;

    @Column(name = "mensaje", columnDefinition = "TEXT")
    public String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    public EstadoAlerta estado = EstadoAlerta.PENDIENTE;

    @Column(name = "fecha_alerta", nullable = false)
    public Instant fechaAlerta = Instant.now();
}
