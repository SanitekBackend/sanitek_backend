package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "temperature")
public class Temperature extends PollutantMeasurementEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;

    public Temperature() {
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }
}
