package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.request.CreateUserRequest;
import org.acme.dto.response.UserResponse;
import org.acme.exception.AppException;
import org.acme.infrastructure.auth.Authenticated;
import org.acme.service.CurrentUserService;
import org.acme.service.UserService;

import java.util.List;

@Authenticated
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject UserService service;
    @Inject CurrentUserService currentUserService;

    @GET
    public List<UserResponse> listAll() {
        currentUserService.requireSuperAdmin();
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    public UserResponse getById(@PathParam("id") Long id) {
        currentUserService.requireSuperAdmin();
        return service.getById(id);
    }
}
