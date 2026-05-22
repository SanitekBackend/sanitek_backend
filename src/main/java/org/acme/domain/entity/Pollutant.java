package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pollutant")
public class Pollutant extends BaseEntity {

    @Column(name = "nomenclature", nullable = false, length = 20)
    private String nomenclature;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pollutant_unit", nullable = false)
    private PollutantUnit pollutantUnit;

    public Pollutant() {
    }

    public String getNomenclature() {
        return nomenclature;
    }

    public void setNomenclature(String nomenclature) {
        this.nomenclature = nomenclature;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PollutantUnit getPollutantUnit() {
        return pollutantUnit;
    }

    public void setPollutantUnit(PollutantUnit pollutantUnit) {
        this.pollutantUnit = pollutantUnit;
    }
}