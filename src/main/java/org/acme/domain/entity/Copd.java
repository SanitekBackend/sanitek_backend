package org.acme.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "copd")
public class Copd extends HealthRecordEntity {

    public Copd() {
    }
}