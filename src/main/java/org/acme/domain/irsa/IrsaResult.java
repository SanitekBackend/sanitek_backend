package org.acme.domain.irsa;

import java.time.Instant;

public record IrsaResult(
        double score,
        String category,
        Instant calculatedAt
) {
    public static IrsaResult of(double score, Instant calculatedAt) {
        return new IrsaResult(score, categorize(score), calculatedAt);
    }

    private static String categorize(double score) {
        if (score >= 80) return "BUENO";
        if (score >= 60) return "MODERADO";
        if (score >= 40) return "MALO";
        return "MUY_MALO";
    }
}
