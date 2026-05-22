package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pollutant_unit")
public class PollutantUnit extends BaseEntity {

    @Column(name = "notation", length = 30)
    private String notation;

    public PollutantUnit(){
    }

    public String getNotation(){
        return notation;
    }

    public void setNotation(String notation){
        this.notation = notation;
    }
}
