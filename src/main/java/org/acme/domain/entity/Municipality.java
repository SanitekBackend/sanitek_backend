package org.acme.domain.entity;

import jakarta.persistence.*;

import java.awt.*;
import java.util.List;

@Entity
@Table(name = "municipality")
public class Municipality extends BaseEntity {

    @Column(name = "municipality_name", nullable = false, unique = true, length = 100)
    private String municipalityName;

    @Column(name = "social_vulnerability")
    private Float socialVulnerability;

    @Column(name = "social_index", length = 50)
    private String socialIndex;

    @OneToMany(mappedBy = "municipality", fetch = FetchType.LAZY)
    private List<Station> stations;

    @OneToMany(mappedBy = "municipality", fetch = FetchType.LAZY)
    private List<Copd> copd_patients;

    @OneToMany(mappedBy = "municipality", fetch = FetchType.LAZY)
    private List<Asthma> asthma_patients;

    @OneToMany(mappedBy = "municipality", fetch = FetchType.LAZY)
    private List<Pneumonia> pneumonia_patients;

    @OneToMany(mappedBy = "municipality", fetch = FetchType.LAZY)
    private List<Smoking> smoking_patients;


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

    public String getSocialIndex() {
        return socialIndex;
    }

    public void setSocialIndex(String socialIndex) {
        this.socialIndex = socialIndex;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public List<Copd> getCopdPatients() {
        return copd_patients;
    }

    public void setCopdPatients(List<Copd> copdPatients) {
        this.copd_patients = copdPatients;
    }

    public List<Asthma> getAsthmaPatients() {
        return asthma_patients;
    }

    public void setAsthmaPatients(List<Asthma> asthmaPatients) {
        this.asthma_patients = asthmaPatients;
    }

    public List<Pneumonia> getPneumoniaPatients() {
        return pneumonia_patients;
    }

    public void setPneumoniaPatients(List<Pneumonia> pneumoniaPatients) {
        this.pneumonia_patients = pneumoniaPatients;
    }

    public List<Smoking> getSmokingPatients(){
        return smoking_patients;
    }

    public void setSmokingPatients(List<Smoking> smoking_patients){
        this.smoking_patients = smoking_patients;
    }
}
