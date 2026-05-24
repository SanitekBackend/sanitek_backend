package org.acme.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Registro persistido de un cálculo IRSA para un municipio.
 *
 * <p>Además del valor final ({@code irsaValue}) y el nivel de riesgo
 * ({@code riskLevel}), se almacena el desglose completo del algoritmo
 * en columnas nullable, de modo que el endpoint de diagnóstico pueda
 * leerlos sin necesidad de recalcular.</p>
 *
 * <p>Las columnas de desglose son opcionales (nullable = true) para que
 * la estrategia {@code hibernate.hbm2ddl.auto=update} pueda agregarlas
 * a una tabla existente sin romper filas históricas.</p>
 */
@Entity
@Table(name = "irsa")
public class Irsa extends BaseEntity {

    // ── Relación con municipio ────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id", nullable = false)
    private Municipality municipality;

    // ── Resultado principal ───────────────────────────────────────────────
    /** Puntuación IRSA final en rango [0, 100]. */
    @Column(name = "irsa_value", nullable = false)
    private Float irsaValue;

    /** LOW / MODERATE / HIGH / CRITICAL */
    @Column(name = "risk_level", nullable = false, length = 50)
    private String riskLevel;

    @Column(name = "is_forecast", nullable = false)
    private Boolean isForecast = false;

    @Column(name = "forecast_date")
    private Instant forecastDate;

    // ── Desglose de contaminantes normalizados [0, 1] ─────────────────────
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

    /** Índice de contaminantes ponderado ∈ [0, 1]. */
    @Column(name = "contaminantes")
    private Double contaminantes;

    // ── Prevalencias epidemiológicas [0, 1] ───────────────────────────────
    @Column(name = "prev_copd")
    private Double prevCopd;

    @Column(name = "prev_asthma")
    private Double prevAsthma;

    @Column(name = "prev_pneumonia")
    private Double prevPneumonia;

    @Column(name = "prev_smoking")
    private Double prevSmoking;

    /** Factor de Vulnerabilidad ∈ [1.0, 2.0]. */
    @Column(name = "factor_vulnerabilidad")
    private Double factorVulnerabilidad;

    // ── Constructores ─────────────────────────────────────────────────────
    public Irsa() {}

    // ── Getters / Setters ─────────────────────────────────────────────────

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

    public Double getContaminantes() { return contaminantes; }
    public void setContaminantes(Double contaminantes) { this.contaminantes = contaminantes; }

    public Double getPrevCopd() { return prevCopd; }
    public void setPrevCopd(Double prevCopd) { this.prevCopd = prevCopd; }

    public Double getPrevAsthma() { return prevAsthma; }
    public void setPrevAsthma(Double prevAsthma) { this.prevAsthma = prevAsthma; }

    public Double getPrevPneumonia() { return prevPneumonia; }
    public void setPrevPneumonia(Double prevPneumonia) { this.prevPneumonia = prevPneumonia; }

    public Double getPrevSmoking() { return prevSmoking; }
    public void setPrevSmoking(Double prevSmoking) { this.prevSmoking = prevSmoking; }

    public Double getFactorVulnerabilidad() { return factorVulnerabilidad; }
    public void setFactorVulnerabilidad(Double factorVulnerabilidad) { this.factorVulnerabilidad = factorVulnerabilidad; }
}
