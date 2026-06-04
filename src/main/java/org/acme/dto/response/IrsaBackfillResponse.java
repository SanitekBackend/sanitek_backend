package org.acme.dto.response;

import java.time.Instant;
import java.util.List;

public record IrsaBackfillResponse(
        Instant from,
        Instant to,
        int months,
        int municipalities,
        int created,
        int updated,
        int skipped,
        int failed,
        List<String> errors
) {
}
