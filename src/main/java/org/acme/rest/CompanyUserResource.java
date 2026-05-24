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
import org.acme.dto.request.CreateCompanyUserRequest;
import org.acme.dto.response.UserResponse;
import org.acme.infrastructure.auth.Authenticated;
import org.acme.service.AdminManagementService;

import java.util.List;

@Authenticated
@Path("/api/company-users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyUserResource {

    @Inject AdminManagementService service;

    @POST
    public Response create(@Valid CreateCompanyUserRequest request) {
        UserResponse response = service.createCompanyUser(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    public List<UserResponse> listAll() {
        return service.listCompanyUsers();
    }

    @PUT
    @Path("/{id}/deactivate")
    public Response deactivate(@PathParam("id") Long id) {
        service.deactivateCompanyUser(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/activate")
    public UserResponse activate(@PathParam("id") Long id) {
        return service.activateCompanyUser(id);
    }
}
