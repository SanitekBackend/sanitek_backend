package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "estacion_meteorologica")
public class EstacionMeteorologica {

    @Id
    @Column(name = "clave_estacion", length = 50)
    public String claveEstacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alcaldia")
    public Alcaldia alcaldia;

    @Column(name = "estacion_nombre", length = 150)
    public String estacionNombre;

    @Column(name = "latitud")
    public Float latitud;

    @Column(name = "longitud")
    public Float longitud;

    @Column(name = "altitud")
    public Integer altitud;

    @Column(name = "clase_estacion", length = 100)
    public String claseEstacion;

    @Column(name = "organismo", length = 100)
    public String organismo;
}
