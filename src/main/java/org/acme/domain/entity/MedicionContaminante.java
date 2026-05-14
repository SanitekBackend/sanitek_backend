package org.acme.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

// Tabla unificada que unifica a cada una de contaminante (NO2, O3, PM_2_5)
@Entity
@Table(name = "medicion_contaminante",
        indexes = {
            @Index(name = "idx_mc_station_fecha", columnList = "id_station, fecha"),
            @Index(name = "idx_mc_contaminante", columnList = "id_contaminante")
        })
@AttributeOverride(name = "id", column = @Column(name = "id_registro"))
public class MedicionContaminante extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_station", nullable = false)
    public Estacion estacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contaminante", nullable = false)
    public ContaminanteParametro contaminante;

    @Column(name = "fecha", nullable = false)
    public Instant fecha;

    @Column(name = "valor_medicion")
    public Float valorMedicion;
}
