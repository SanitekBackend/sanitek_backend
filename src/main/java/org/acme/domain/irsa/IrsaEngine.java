package org.acme.domain.irsa;

import java.time.Instant;

public class IrsaEngine {

    public IrsaResult calculate(
            double avgNo2,
            double avgO3,
            double avgPm25,
            double avgUv,
            double avgTmp,
            long   copdCount,
            long   asthmaCount,
            long   pneumoniaCount,
            long   smokingCount) {

        double normNo2  = norm(avgNo2,  IrsaWeightConfig.THRESHOLD_NO2);
        double normO3   = norm(avgO3,   IrsaWeightConfig.THRESHOLD_O3);
        double normPm25 = norm(avgPm25, IrsaWeightConfig.THRESHOLD_PM25);
        double normUv   = norm(avgUv,   IrsaWeightConfig.THRESHOLD_UV);
        double normTmp  = norm(avgTmp,  IrsaWeightConfig.THRESHOLD_TMP);

        double contaminantes = (IrsaWeightConfig.W_NO2  * normNo2)
                             + (IrsaWeightConfig.W_O3   * normO3)
                             + (IrsaWeightConfig.W_PM25 * normPm25)
                             + (IrsaWeightConfig.W_UV   * normUv)
                             + (IrsaWeightConfig.W_TMP  * normTmp);

        long total = Math.max(copdCount + asthmaCount + pneumoniaCount + smokingCount, 1L);

        double prevCopd      = Math.min((double) copdCount      / total, 1.0);
        double prevAsthma    = Math.min((double) asthmaCount    / total, 1.0);
        double prevPneumonia = Math.min((double) pneumoniaCount / total, 1.0);
        double prevSmoking   = Math.min((double) smokingCount   / total, 1.0);

        double factorVulnerabilidad = 1.0
                + (IrsaWeightConfig.W_COPD      * prevCopd)
                + (IrsaWeightConfig.W_ASTHMA    * prevAsthma)
                + (IrsaWeightConfig.W_PNEUMONIA * prevPneumonia)
                + (IrsaWeightConfig.W_SMOKING   * prevSmoking);

        double irsaScore = contaminantes * (factorVulnerabilidad / 2.0) * 100.0;
        irsaScore = Math.max(0.0, Math.min(100.0, irsaScore));

        return new IrsaResult(
                round4(normNo2),
                round4(normO3),
                round4(normPm25),
                round4(normUv),
                round4(normTmp),
                round4(contaminantes),
                round4(prevCopd),
                round4(prevAsthma),
                round4(prevPneumonia),
                round4(prevSmoking),
                round4(factorVulnerabilidad),
                round4(irsaScore),
                IrsaResult.categorize(irsaScore),
                Instant.now()
        );
    }

    private static double norm(double value, double threshold) {
        if (threshold <= 0.0 || value <= 0.0) return 0.0;
        return Math.min(value / threshold, 1.0);
    }

    private static double round4(double v) {
        return Math.round(v * 10_000.0) / 10_000.0;
    }
}
