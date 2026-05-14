package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "estacion")
public class Estacion {

    @Id
    @Column(name = "id_station", length = 50)
    public String idStation;

    @Column(name = "estacion_nombre", length = 150)
    public String estacionNombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alcaldia")
    public Alcaldia alcaldia;

    @Column(name = "latitud")
    public Float latitud;

    @Column(name = "longitud")
    public Float longitud;
}
