package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "climas")
public class Clima extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alcaldia")
    public Alcaldia alcaldia;

    @Column(name = "altitud")
    public Double altitud;

    @Column(name = "latitud")
    public Double latitud;

    @Column(name = "area")
    public Double area;

    @Column(name = "perimetro")
    public Double perimetro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clave")
    public CatalogoClima catalogoClima;
}
