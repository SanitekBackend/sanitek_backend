package org.acme.domain.irsa;

public final class IrsaWeightConfig {

    private IrsaWeightConfig() {}

    public static final double W_NO2  = 0.35;
    public static final double W_O3   = 0.30;
    public static final double W_PM25 = 0.20;
    public static final double W_UV   = 0.08;
    public static final double W_TMP  = 0.07;

    public static final double THRESHOLD_NO2  = 100.0;
    public static final double THRESHOLD_O3   = 110.0;
    public static final double THRESHOLD_PM25 = 45.0;
    public static final double THRESHOLD_UV   = 11.0;
    public static final double THRESHOLD_TMP  = 40.0;

    public static final double W_COPD      = 0.35;
    public static final double W_ASTHMA    = 0.30;
    public static final double W_PNEUMONIA = 0.25;
    public static final double W_SMOKING   = 0.10;
}
