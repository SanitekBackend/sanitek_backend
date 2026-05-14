package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
@AttributeOverride(name = "id", column = @Column(name = "id_rol"))
public class Rol extends BaseEntity {

    @Column(name = "nombre_rol", nullable = false, unique = true, length = 50)
    public String nombreRol;
}
