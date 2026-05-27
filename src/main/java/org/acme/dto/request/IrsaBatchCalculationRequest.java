package org.acme.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record IrsaBatchCalculationRequest(
        @NotEmpty
        List<Long> municipalityIds
) {
}
