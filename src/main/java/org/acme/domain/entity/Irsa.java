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

    @Column(name = "period_start")
    private Instant periodStart;

    @Column(name = "period_end")
    private Instant periodEnd;

    @Column(name = "norm_no2")
    private Double normNo2;

    @Column(name = "norm_o3")
    private Double normO3;

    @Column(name = "norm_pm25")
    private Double normPm25;

    @Column(name = "norm_uv")
    private Double normUv;

    @Column(name = "norm_tmp")
    private Double normTmp;

    @Column(name = "pollutant_score")
    private Double pollutantScore;

    @Column(name = "prev_copd")
    private Double prevCopd;

    @Column(name = "prev_asthma")
    private Double prevAsthma;

    @Column(name = "prev_pneumonia")
    private Double prevPneumonia;

    @Column(name = "prev_smoking")
    private Double prevSmoking;

    @Column(name = "vulnerability_factor")
    private Double vulnerabilityFactor;

    public Irsa() {}


    public Municipality getMunicipality() { return municipality; }
    public void setMunicipality(Municipality municipality) { this.municipality = municipality; }

    public Float getIrsaValue() { return irsaValue; }
    public void setIrsaValue(Float irsaValue) { this.irsaValue = irsaValue; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Boolean getIsForecast() { return isForecast; }
    public void setIsForecast(Boolean isForecast) { this.isForecast = isForecast; }

    public Instant getForecastDate() { return forecastDate; }
    public void setForecastDate(Instant forecastDate) { this.forecastDate = forecastDate; }

    public Instant getPeriodStart() { return periodStart; }
    public void setPeriodStart(Instant periodStart) { this.periodStart = periodStart; }

    public Instant getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(Instant periodEnd) { this.periodEnd = periodEnd; }

    public Double getNormNo2() { return normNo2; }
    public void setNormNo2(Double normNo2) { this.normNo2 = normNo2; }

    public Double getNormO3() { return normO3; }
    public void setNormO3(Double normO3) { this.normO3 = normO3; }

    public Double getNormPm25() { return normPm25; }
    public void setNormPm25(Double normPm25) { this.normPm25 = normPm25; }

    public Double getNormUv() { return normUv; }
    public void setNormUv(Double normUv) { this.normUv = normUv; }

    public Double getNormTmp() { return normTmp; }
    public void setNormTmp(Double normTmp) { this.normTmp = normTmp; }

    public Double getPollutantScore() { return pollutantScore; }
    public void setPollutantScore(Double pollutantScore) { this.pollutantScore = pollutantScore; }

    public Double getPrevCopd() { return prevCopd; }
    public void setPrevCopd(Double prevCopd) { this.prevCopd = prevCopd; }

    public Double getPrevAsthma() { return prevAsthma; }
    public void setPrevAsthma(Double prevAsthma) { this.prevAsthma = prevAsthma; }

    public Double getPrevPneumonia() { return prevPneumonia; }
    public void setPrevPneumonia(Double prevPneumonia) { this.prevPneumonia = prevPneumonia; }

    public Double getPrevSmoking() { return prevSmoking; }
    public void setPrevSmoking(Double prevSmoking) { this.prevSmoking = prevSmoking; }

    public Double getVulnerabilityFactor() { return vulnerabilityFactor; }
    public void setVulnerabilityFactor(Double vulnerabilityFactor) { this.vulnerabilityFactor = vulnerabilityFactor; }
}
