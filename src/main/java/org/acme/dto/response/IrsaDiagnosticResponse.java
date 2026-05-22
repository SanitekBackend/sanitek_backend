package org.acme.dto.response;

import java.util.Map;

public record IrsaDiagnosticResponse(
        Long municipalityId,
        String municipalityName,
        double airScore,
        double climateScore,
        double socioScore,
        double healthScore,
        double weightedScore,
        double irsaValue,
        String riskLevel,
        int no2Measurements,
        int o3Measurements,
        int pm25Measurements,
        Map<String, Double> averagesByPollutant,
        boolean hasTemperatureData,
        String socialIndex,
        Float socialVulnerability
) {}
