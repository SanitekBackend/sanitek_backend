package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.request.CreateUserRequest;
import org.acme.dto.request.UpdateRoleRequest;
import org.acme.dto.response.UserResponse;
import org.acme.service.UserService;

import java.util.List;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject UserService service;

    @GET
    public List<UserResponse> listAll() {
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    public UserResponse getById(@PathParam("id") Long id) {
        return service.getById(id);
    }

    @POST
    public Response register(@Valid CreateUserRequest request) {
        UserResponse response = service.register(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}/role")
    public UserResponse updateRole(@PathParam("id") Long id, @Valid UpdateRoleRequest request) {
        return service.updateRole(id, request);
    }
}
