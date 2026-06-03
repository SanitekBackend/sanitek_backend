package org.acme.dto.response;

import java.util.List;

public record IrsaTimelineSnapshotResponse(
        int offsetDays,
        String label,
        List<IrsaTimelineMunicipalityPoint> municipalities
) {
}
