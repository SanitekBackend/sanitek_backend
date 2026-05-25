package org.acme.domain.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "municipality")
public class Municipality extends BaseEntity {

    @Column(name = "municipality_name", nullable = false, unique = true, length = 100)
    private String municipalityName;

    @Column(name = "social_vulnerability")
    private Float socialVulnerability;

    @OneToMany(mappedBy = "municipality", fetch = FetchType.LAZY)
    private List<Station> stations;

    @OneToMany(mappedBy = "municipality", fetch = FetchType.LAZY)
    private List<Copd> copdPatients;

    @OneToMany(mappedBy = "municipality", fetch = FetchType.LAZY)
    private List<Asthma> asthmaPatients;

    @OneToMany(mappedBy = "municipality", fetch = FetchType.LAZY)
    private List<Pneumonia> pneumoniaPatients;

    @OneToMany(mappedBy = "municipality", fetch = FetchType.LAZY)
    private List<Smoking> smokingPatients;


    public Municipality() {
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }

    public Float getSocialVulnerability() {
        return socialVulnerability;
    }

    public void setSocialVulnerability(Float socialVulnerability) {
        this.socialVulnerability = socialVulnerability;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public List<Copd> getCopdPatients() {
        return copdPatients;
    }

    public void setCopdPatients(List<Copd> copdPatients) {
        this.copdPatients = copdPatients;
    }

    public List<Asthma> getAsthmaPatients() {
        return asthmaPatients;
    }

    public void setAsthmaPatients(List<Asthma> asthmaPatients) {
        this.asthmaPatients = asthmaPatients;
    }

    public List<Pneumonia> getPneumoniaPatients() {
        return pneumoniaPatients;
    }

    public void setPneumoniaPatients(List<Pneumonia> pneumoniaPatients) {
        this.pneumoniaPatients = pneumoniaPatients;
    }

    public List<Smoking> getSmokingPatients(){
        return smokingPatients;
    }

    public void setSmokingPatients(List<Smoking> smokingPatients){
        this.smokingPatients = smokingPatients;
    }
}
