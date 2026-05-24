package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.request.UpdateProfileRequest;
import org.acme.dto.response.UserResponse;
import org.acme.infrastructure.auth.Authenticated;
import org.acme.service.AdminManagementService;

@Authenticated
@Path("/api/me")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MeResource {

    @Inject AdminManagementService service;

    @GET
    public UserResponse getMe() {
        return service.getMe();
    }

    @PUT
    public UserResponse updateMe(@Valid UpdateProfileRequest request) {
        return service.updateMe(request);
    }
}
