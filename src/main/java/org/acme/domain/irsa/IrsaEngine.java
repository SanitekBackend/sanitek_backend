package org.acme.domain.irsa;

import java.time.Instant;

public class IrsaEngine {

    private final IrsaWeightConfig config;

    public IrsaEngine(IrsaWeightConfig config) {
        this.config = config;
    }

    public IrsaResult calculate(double airQualityScore, double climateScore,
                                double socioeconomicScore, double healthScore) {
        double score = (airQualityScore    * config.airQualityWeight)
                     + (climateScore       * config.climateWeight)
                     + (socioeconomicScore * config.socioeconomicWeight)
                     + (healthScore        * config.healthWeight);
        return IrsaResult.of(score, Instant.now());
    }
}
