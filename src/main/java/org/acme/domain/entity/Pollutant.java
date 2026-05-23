package org.acme.domain.entity;

import jakarta.persistence.*;
import org.checkerframework.checker.units.qual.N;

import java.util.List;

@Entity
@Table(name = "pollutant")
public class Pollutant extends BaseEntity {

    @Column(name = "nomenclature", nullable = false, unique = true, length = 20)
    private String nomenclature;

    @Column(name = "name", length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pollutant_unit_id")
    private PollutantUnit pollutantUnit;

    @OneToMany(mappedBy = "pollutant", fetch = FetchType.LAZY)
    private List<O3> o3s;

    @OneToMany(mappedBy = "pollutant", fetch = FetchType.LAZY)
    private List<NO2> no2s;

    @OneToMany(mappedBy = "pollutant", fetch = FetchType.LAZY)
    private List<PM25> pm25s;


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

    public List<O3> getO3s() {
        return o3s;
    }

    public void setO3s(List<O3> o3s) {
        this.o3s = o3s;
    }

    public List<NO2> getNo2s() {
        return no2s;
    }

    public void setNO2s(List<NO2> no2s) {
        this.no2s = no2s;
    }

    public List<PM25> getPm25s() {
        return pm25s;
    }

    public void setPm25s(List<PM25> pm25s) {
        this.pm25s = pm25s;
    }
}
