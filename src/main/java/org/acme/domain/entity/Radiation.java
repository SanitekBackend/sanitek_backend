package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "radiation")
public class Radiation extends PollutantMeasurementEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    public Radiation() {
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }
}