package org.acme.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "epidemiologico")
@AttributeOverride(name = "id", column = @Column(name = "id_registro"))
public class Epidemiologico extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alcaldia", nullable = false)
    public Alcaldia alcaldia;

    @Column(name = "fecha_ingreso")
    public LocalDate fechaIngreso;

    @Column(name = "edad")
    public Integer edad;

    @Column(name = "epoc")
    public Boolean epoc;

    @Column(name = "asma")
    public Boolean asma;

    @Column(name = "tabaquismo")
    public Boolean tabaquismo;

    @Column(name = "resultado_pcr")
    public Boolean resultadoPcr;
}
