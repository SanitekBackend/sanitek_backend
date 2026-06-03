package org.acme.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "smoking")
public class Smoking extends HealthRecordEntity {

    public Smoking() {
    }
}