package org.acme.dto.request;

import jakarta.validation.constraints.*;
import jakarta.ws.rs.QueryParam;
import java.time.Instant;

public class FiltroMedicionRequest {

    @QueryParam("id_station")
    public String idStation;

    @QueryParam("id_contaminante")
    public Integer idContaminante;

    @NotNull
    @QueryParam("desde")
    public Instant desde;

    @NotNull
    @QueryParam("hasta")
    public Instant hasta;
}
