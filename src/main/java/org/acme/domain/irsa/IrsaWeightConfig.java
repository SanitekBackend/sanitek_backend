package org.acme.domain.irsa;

public class IrsaWeightConfig {

    public final double airQualityWeight;
    public final double climateWeight;
    public final double socioeconomicWeight;
    public final double healthWeight;

    public IrsaWeightConfig(double airQualityWeight, double climateWeight,
                            double socioeconomicWeight, double healthWeight) {
        if (Math.abs(airQualityWeight + climateWeight + socioeconomicWeight + healthWeight - 1.0) > 0.0001) {
            throw new IllegalArgumentException("Los pesos deben sumar 1.0");
        }
        this.airQualityWeight    = airQualityWeight;
        this.climateWeight       = climateWeight;
        this.socioeconomicWeight = socioeconomicWeight;
        this.healthWeight        = healthWeight;
    }

    // Aire 35% | Clima 25% | Socioeconómico 20% | Salud 20%
    public static IrsaWeightConfig defaults() {
        return new IrsaWeightConfig(0.35, 0.25, 0.20, 0.20);
    }
}
