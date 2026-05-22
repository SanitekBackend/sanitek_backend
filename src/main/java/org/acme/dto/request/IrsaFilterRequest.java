package org.acme.dto.request;

import jakarta.ws.rs.QueryParam;
import java.time.Instant;

public class IrsaFilterRequest {

    @QueryParam("municipalityId")
    public Long municipalityId;

    @QueryParam("riskLevel")
    public String riskLevel;

    @QueryParam("from")
    public Instant from;

    @QueryParam("to")
    public Instant to;
}
