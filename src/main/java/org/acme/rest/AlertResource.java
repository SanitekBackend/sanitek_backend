package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.request.CreateAlertRequest;
import org.acme.dto.response.AlertResponse;
import org.acme.service.AlertService;

import java.util.List;

@Path("/api/alerts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlertResource {

    @Inject AlertService service;

    @POST
    public Response create(
            @QueryParam("userId") Long userId,
            @Valid CreateAlertRequest request) {
        AlertResponse response = service.create(userId, request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("/subscribe")
    public Response subscribe(
            @QueryParam("userId") Long userId,
            @QueryParam("municipalityId") Long municipalityId) {
        AlertResponse response = service.subscribe(userId, municipalityId);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/user/{userId}")
    public List<AlertResponse> getByUser(@PathParam("userId") Long userId) {
        return service.getByUser(userId);
    }

    @GET
    @Path("/user/{userId}/active")
    public List<AlertResponse> getActiveByUser(@PathParam("userId") Long userId) {
        return service.getActiveByUser(userId);
    }

    @GET
    @Path("/municipality/{municipalityId}")
    public List<AlertResponse> getByMunicipality(@PathParam("municipalityId") Long municipalityId) {
        return service.getByMunicipality(municipalityId);
    }

    @PUT
    @Path("/{id}/activate")
    public AlertResponse activate(@PathParam("id") Long id) {
        return service.activate(id);
    }

    @PUT
    @Path("/{id}/deactivate")
    public Response deactivate(@PathParam("id") Long id) {
        service.deactivate(id);
        return Response.noContent().build();
    }
}
