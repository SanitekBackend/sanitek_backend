package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "dato_salud_agregado",
        indexes = @Index(name = "idx_salud_alcaldia_periodo", columnList = "id_alcaldia, anio, mes"))
@AttributeOverride(name = "id", column = @Column(name = "id_dato_salud"))
public class DatoSaludAgregado extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alcaldia", nullable = false)
    public Alcaldia alcaldia;

    @Column(name = "anio", nullable = false)
    public Integer anio;

    @Column(name = "mes", nullable = false)
    public Integer mes;

    @Column(name = "total_casos", nullable = false)
    public Integer totalCasos;

    @Column(name = "total_defunciones", nullable = false)
    public Integer totalDefunciones;

    @Column(name = "casos_neumonia", nullable = false)
    public Integer casosNeumonia;

    @Column(name = "casos_epoc", nullable = false)
    public Integer casosEpoc;

    @Column(name = "casos_asma", nullable = false)
    public Integer casosAsma;

    @Column(name = "casos_tabaquismo", nullable = false)
    public Integer casosTabaquismo;

    @Column(name = "promedio_edad")
    public Double promedioEdad;
}
