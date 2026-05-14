package org.acme.domain.entity;

import jakarta.persistence.*;
import org.acme.domain.enums.PollutantType;

@Entity
@Table(name = "contaminantes")
public class ContaminanteEntity extends BaseEntity {

    @Column(name = "municipio", nullable = false)
    public String municipio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cont", nullable = false)
    public PollutantType tipoCont;

    @Column(name = "valor_medicion", nullable = false)
    public Float valorMedicion;

    @Column(name = "anio", nullable = false)
    public Integer anio;

    @Column(name = "codigo_estacion")
    public String codigoEstacion;

    public ContaminanteEntity() {}

    public ContaminanteEntity(String municipio, PollutantType tipoCont, Float valorMedicion, Integer anio, String codigoEstacion) {
        this.municipio = municipio;
        this.tipoCont = tipoCont;
        this.valorMedicion = valorMedicion;
        this.anio = anio;
        this.codigoEstacion = codigoEstacion;
    }
}
