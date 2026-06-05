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
        if (score <= 35.0) return "LOW";
        if (score <= 39.0) return "MODERATE";
        return "HIGH";
    }
}
