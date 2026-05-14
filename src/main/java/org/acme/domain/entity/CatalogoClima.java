package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "catalogo_clima")
public class CatalogoClima {

    @Id
    @Column(name = "clave", length = 50)
    public String clave;

    @Column(name = "tipo_clima", length = 150)
    public String tipoClima;
}
