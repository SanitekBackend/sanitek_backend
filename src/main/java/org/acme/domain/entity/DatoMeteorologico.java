package org.acme.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

//radiacion_solar
@Entity
@Table(name = "dato_meteorologico",
        indexes = @Index(name = "idx_dm_estacion_fecha", columnList = "clave_estacion, fecha"))
@AttributeOverride(name = "id", column = @Column(name = "id_registro"))
public class DatoMeteorologico extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clave_estacion", nullable = false)
    public EstacionMeteorologica estacionMeteorologica;

    @Column(name = "fecha", nullable = false)
    public Instant fecha;

    @Column(name = "humedad_relativa")
    public Float humedadRelativa;

    @Column(name = "temperatura_ambiental")
    public Float temperaturaAmbiental;

    @Column(name = "direccion_viento")
    public Float direccionViento;

    @Column(name = "velocidad_viento")
    public Float velocidadViento;
}
