package org.acme.domain.entity;

import jakarta.persistence.*;

//unidades de medición 
@Entity
@Table(name = "contaminante_unidades")
@AttributeOverride(name = "id", column = @Column(name = "id_medicion"))
public class ContaminanteUnidad extends BaseEntity {

    @Column(name = "notacion", length = 30)
    public String notacion;

    @Column(name = "nombre", length = 100)
    public String nombre;
}
