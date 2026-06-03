package org.acme.dto.response;

public record TrendPoint(
        String label,
        double avgIrsa,
        double minIrsa,
        double maxIrsa,
        String riskLevel,
        int count
) {}
