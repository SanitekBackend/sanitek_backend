package org.acme.domain.entity;

import jakarta.persistence.*;
import org.acme.domain.enums.NivelRiesgo;
import java.time.Instant;

@Entity
@Table(name = "irsa",
        indexes = @Index(name = "idx_irsa_alcaldia_fecha", columnList = "id_alcaldia, fecha_calculo"))
@AttributeOverride(name = "id", column = @Column(name = "id_irsa"))
public class Irsa extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alcaldia", nullable = false)
    public Alcaldia alcaldia;

    @Column(name = "valor_irsa", nullable = false)
    public Float valorIrsa;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_riesgo", nullable = false, length = 20)
    public NivelRiesgo nivelRiesgo;

    @Column(name = "fecha_calculo", nullable = false)
    public Instant fechaCalculo = Instant.now();

    @Column(name = "prediccion_futura")
    public Float prediccionFutura;

    @Column(name = "fecha_prediccion")
    public Instant fechaPrediccion;

    // 'SCHEDULER' 
    @Column(name = "origen_calculo", length = 50)
    public String origenCalculo = "SCHEDULER";
}
