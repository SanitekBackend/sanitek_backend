package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.response.MunicipalityResponse;
import org.acme.service.MunicipalityService;

import java.util.List;

@Path("/api/municipalities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MunicipalityResource {

    @Inject MunicipalityService service;

    @GET
    public List<MunicipalityResponse> getAll() {
        return service.getAll();
    }

    @GET
    @Path("/{id}")
    public MunicipalityResponse getById(@PathParam("id") Long id) {
        return service.getById(id);
    }
}
