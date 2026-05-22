package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "municipality")
public class Municipality extends BaseEntity {

    @Column(name = "municipality_name", nullable = false, length = 100)
    private String municipalityName;

    @Column(name = "social_vulnerability")
    private Float socialVulnerability;

    @Column(name = "social_index", length = 50)
    private String socialIndex;

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
}