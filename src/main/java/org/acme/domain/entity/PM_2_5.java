package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pm25")
public class PM_2_5 extends PollutantMeasurementEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pollutant_id", nullable = false)
    private Pollutant pollutant;

    public PM_2_5() {
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Pollutant getPollutant() {
        return pollutant;
    }

    public void setPollutant(Pollutant pollutant) {
        this.pollutant = pollutant;
    }
}
