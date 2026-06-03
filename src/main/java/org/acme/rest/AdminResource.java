package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.request.CreateAdminRequest;
import org.acme.dto.request.UpdateAdminRequest;
import org.acme.dto.response.UserResponse;
import org.acme.infrastructure.auth.Authenticated;
import org.acme.service.AdminManagementService;

import java.util.List;

@Authenticated
@Path("/api/admins")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {

    @Inject AdminManagementService service;

    @POST
    public Response create(@Valid CreateAdminRequest request) {
        UserResponse response = service.createAdmin(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    public List<UserResponse> listAll() {
        return service.listAdmins();
    }

    @PUT
    @Path("/{id}")
    public UserResponse update(@PathParam("id") Long id, @Valid UpdateAdminRequest request) {
        return service.updateAdmin(id, request);
    }

    @PUT
    @Path("/{id}/deactivate")
    public Response deactivate(@PathParam("id") Long id) {
        service.deactivateAdmin(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/activate")
    public UserResponse activate(@PathParam("id") Long id) {
        return service.activateAdmin(id);
    }
}
