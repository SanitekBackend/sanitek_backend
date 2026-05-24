package org.acme.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "asthma")
public class Asthma extends HealthRecordEntity {

    public Asthma() {
    }
}