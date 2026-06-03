package org.acme.domain.irsa;

import java.time.Instant;

public record IrsaResult(
        double  normNo2,
        double  normO3,
        double  normPm25,
        double  normUv,
        double  normTmp,
        double  pollutantScore,
        double  prevCopd,
        double  prevAsthma,
        double  prevPneumonia,
        double  prevSmoking,
        double  vulnerabilityFactor,
        double  irsaScore,
        String  riskLevel,
        Instant calculatedAt
) {
    public static String categorize(double score) {
        if (score <= 40.0) return "LOW";       // 0-40  → IRSA bajo   (verde)
        if (score <= 70.0) return "MODERATE";  // 41-70 → IRSA regular (amarillo)
        return "HIGH";                          // 71-100→ IRSA alto   (rojo)
    }
}
