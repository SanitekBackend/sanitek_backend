package org.acme.domain.entity;

import jakarta.persistence.*;
import org.acme.domain.enums.NivelRezago;

@Entity
@Table(name = "alcaldias")
@AttributeOverride(name = "id", column = @Column(name = "id_alcaldia"))
public class Alcaldia extends BaseEntity {

    @Column(name = "nombre", nullable = false, length = 100)
    public String nombre;

    @Column(name = "indice_rezago_social")
    public Float indiceRezagoSocial;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_rezago", length = 20)
    public NivelRezago nivelRezago;
}
