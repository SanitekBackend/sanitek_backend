package org.acme.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "irsa")
public class Irsa extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id", nullable = false)
    private Municipality municipality;

    @Column(name = "irsa_value", nullable = false)
    private Float irsaValue;

    @Column(name = "risk_level", nullable = false, length = 50)
    private String riskLevel;

    @Column(name = "is_forecast", nullable = false)
    private Boolean isForecast = false;

    @Column(name = "forecast_date")
    private Instant forecastDate;

    public Irsa() {
    }

    public Municipality getMunicipality() {
        return municipality;
    }

    public void setMunicipality(Municipality municipality) {
        this.municipality = municipality;
    }

    public Float getIrsaValue() {
        return irsaValue;
    }

    public void setIrsaValue(Float irsaValue) {
        this.irsaValue = irsaValue;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Boolean getIsForecast() {
        return isForecast;
    }

    public void setIsForecast(Boolean isForecast) {
        this.isForecast = isForecast;
    }

    public Instant getForecastDate() {
        return forecastDate;
    }

    public void setForecastDate(Instant forecastDate) {
        this.forecastDate = forecastDate;
    }
}