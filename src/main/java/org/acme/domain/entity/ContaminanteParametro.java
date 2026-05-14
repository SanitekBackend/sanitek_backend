package org.acme.domain.entity;

import jakarta.persistence.*;

//O3, PM10, PM2.5
@Entity
@Table(name = "contaminante_parametro")
@AttributeOverride(name = "id", column = @Column(name = "id_contaminante"))
public class ContaminanteParametro extends BaseEntity {

    @Column(name = "nomenclatura", nullable = false, unique = true, length = 20)
    public String nomenclatura;

    @Column(name = "nombre", length = 100)
    public String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medicion")
    public ContaminanteUnidad unidad;
}
