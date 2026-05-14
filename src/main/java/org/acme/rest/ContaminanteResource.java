package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.response.ContaminanteAnualResponse;
import org.acme.service.ContaminanteService;

import java.util.List;

@Path("/api/contaminantes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContaminanteResource {

    @Inject
    ContaminanteService service;

    @GET
    @Path("/anual")
    public List<ContaminanteAnualResponse> getDatosAnuales(
            @QueryParam("municipio") String municipio,
            @QueryParam("anio") Integer anio) {
        return service.getDatosAnuales(municipio, anio);
    }
}
