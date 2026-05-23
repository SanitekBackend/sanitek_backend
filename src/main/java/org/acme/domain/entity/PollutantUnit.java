package org.acme.domain.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "pollutant_unit")
public class PollutantUnit extends BaseEntity {

    @Column(name = "notation", unique = true, length = 30)
    private String notation;

    @OneToMany(mappedBy = "pollutant_unit", fetch = FetchType.LAZY)
    private List<Pollutant> pollutants;

    public PollutantUnit(){
    }

    public String getNotation(){
        return notation;
    }

    public void setNotation(String notation){
        this.notation = notation;
    }
}
