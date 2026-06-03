package org.acme.dto.response;

import java.util.List;

public record IrsaTrendResponse(
        Long municipalityId,
        String municipalityName,
        String period,
        int periods,
        String trend,
        double variation,
        List<TrendPoint> points
) {}
