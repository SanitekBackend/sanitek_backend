package org.acme.dto.response;

public record IrsaDiagnosticResponse(
        Long   municipalityId,
        String municipalityName,

        double normNo2,
        double normO3,
        double normPm25,
        double normUv,
        double normTmp,
        double contaminantes,

        double prevCopd,
        double prevAsthma,
        double prevPneumonia,
        double prevSmoking,

        double factorVulnerabilidad,

        double irsaScore,
        String riskLevel,

        int  no2Measurements,
        int  o3Measurements,
        int  pm25Measurements,
        int  uvMeasurements,
        int  tmpMeasurements,
        long copdCount,
        long asthmaCount,
        long pneumoniaCount,
        long smokingCount
) {}
