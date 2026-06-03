package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.response.StationResponse;
import org.acme.service.StationService;

import java.util.List;

@Path("/api/stations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StationResource {

    @Inject StationService service;

    @GET
    public List<StationResponse> listAll() {
        return service.getAll();
    }

    @GET
    @Path("/municipality/{municipalityId}")
    public List<StationResponse> getByMunicipality(@PathParam("municipalityId") Long municipalityId) {
        return service.getByMunicipality(municipalityId);
    }
}
