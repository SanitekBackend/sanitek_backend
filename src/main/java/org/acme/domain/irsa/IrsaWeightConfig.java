package org.acme.domain.irsa;

public final class IrsaWeightConfig {

    private IrsaWeightConfig() {}

    // ── Pesos de contaminantes (suma = 1.0) ─────────────────────────────────
    public static final double W_NO2  = 0.35;
    public static final double W_O3   = 0.30;
    public static final double W_PM25 = 0.20;
    public static final double W_UV   = 0.08;
    public static final double W_TMP  = 0.07;

    // ── Umbrales de peligro basados en guías OMS 2021 ───────────────────────
    // Cuando la medición supera el umbral, la norma se clampea a 1.0 y el
    // contaminante contribuye con su peso máximo al puntaje de contaminación.
    //
    //  NO2  : 25 µg/m³  — guía OMS 24 h (era 100 µg/m³, demasiado permisivo)
    //  O3   : 100 µg/m³ — guía OMS 8 h pico
    //  PM2.5: 15 µg/m³  — guía OMS 24 h (era 45 µg/m³, demasiado permisivo)
    //  UV   : índice 6  — umbral riesgo alto (era 11, nunca se alcanzaba)
    //  TMP  : 35 °C     — umbral estrés térmico OMS (era 40 °C)
    public static final double THRESHOLD_NO2  =  25.0;
    public static final double THRESHOLD_O3   = 100.0;
    public static final double THRESHOLD_PM25 =  15.0;
    public static final double THRESHOLD_UV   =   6.0;
    public static final double THRESHOLD_TMP  =  35.0;

    // ── Pesos de vulnerabilidad sanitaria (suma = 1.0) ──────────────────────
    public static final double W_COPD      = 0.35;
    public static final double W_ASTHMA    = 0.30;
    public static final double W_PNEUMONIA = 0.25;
    public static final double W_SMOKING   = 0.10;
}
