package org.acme.domain.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "pollutant_unit")
public class PollutantUnit extends BaseEntity {

    @Column(name = "notation", unique = true, length = 30)
    private String notation;

    @OneToMany(mappedBy = "pollutantUnit", fetch = FetchType.LAZY)
    private List<Pollutant> pollutants;

    public PollutantUnit(){
    }

    public String getNotation(){
        return notation;
    }

    public void setNotation(String notation){
        this.notation = notation;
    }

    public List<Pollutant> getPollutants() {
        return pollutants;
    }

    public void setPollutants(List<Pollutant> pollutants) {
        this.pollutants = pollutants;
    }
}
