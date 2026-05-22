package org.acme.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateRoleRequest(
        @NotNull @Positive Long roleId
) {}
