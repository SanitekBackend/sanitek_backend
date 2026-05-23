package org.acme.domain.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "station")
public class Station extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id")
    private Municipality municipality;

    @Column(name = "station_short_name", nullable = false, unique = true, length = 50)
    private String stationShortName;

    @Column(name = "station_name", nullable = false, unique = true, length = 150)
    private String stationName;

    @OneToMany(mappedBy = "station", fetch = FetchType.LAZY)
    private List<Temperature> temperatures;

    @OneToMany(mappedBy = "station", fetch = FetchType.LAZY)
    private List<NO2> no2s;

    @OneToMany(mappedBy = "station", fetch = FetchType.LAZY)
    private List<O3> o3s;

    @OneToMany(mappedBy = "station", fetch = FetchType.LAZY)
    private List<PM25> pm25s;


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

    public List<Temperature> getTemperatures() {
        return temperatures;
    }

    public void setTemperatures(List<Temperature> temperatures) {
        this.temperatures = temperatures;
    }

    public List<O3> getO3s() {
        return o3s;
    }

    public void setO3s(List<O3> o3s) {
        this.o3s = o3s;
    }

    public List<NO2> getNo2s() {
        return no2s;
    }

    public void setNO2s(List<NO2> no2s) {
        this.no2s = no2s;
    }

    public List<PM25> getPm25s() {
        return pm25s;
    }

    public void setPm25s(List<PM25> pm25s) {
        this.pm25s = pm25s;
    }
}
