package org.acme.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "pneumonia")
public class Pneumonia extends HealthRecordEntity {

    public Pneumonia() {
    }
}