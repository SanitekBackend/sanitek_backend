package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "station")
public class Station extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id", nullable = false)
    private Municipality municipality;

    @Column(name = "station_short_name", nullable = false, length = 50)
    private String stationShortName;

    @Column(name = "station_name", nullable = false, length = 150)
    private String stationName;

    public Station() {
    }

    public Municipality getMunicipality() {
        return municipality;
    }

    public void setMunicipality(Municipality municipality) {
        this.municipality = municipality;
    }

    public String getStationShortName() {
        return stationShortName;
    }

    public void setStationShortName(String stationShortName) {
        this.stationShortName = stationShortName;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }
}