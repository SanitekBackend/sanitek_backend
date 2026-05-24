package org.acme.dto.response;

import java.time.Instant;

public record IrsaSummary(Float irsaValue, String riskLevel, Instant calculatedAt) {}
