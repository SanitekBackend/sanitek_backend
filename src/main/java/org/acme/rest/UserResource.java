package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.response.CompanySummary;
import org.acme.dto.response.RoleResponse;
import org.acme.dto.response.UserResponse;
import org.acme.exception.AppException;
import org.acme.infrastructure.auth.Authenticated;
import org.acme.service.CurrentUserService;
import org.acme.service.UserService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@Authenticated
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject UserService service;
    @Inject CurrentUserService currentUserService;
    @ConfigProperty(name = "sanitek.mock-users.enabled", defaultValue = "false")
    boolean mockUsersEnabled;

    @GET
    public List<UserResponse> listAll() {
        if (mockUsersEnabled) {
            return mockUsers();
        }
        currentUserService.requireSuperAdmin();
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    public UserResponse getById(@PathParam("id") Long id) {
        if (mockUsersEnabled) {
            return mockUsers().stream()
                    .filter(user -> user.id().equals(id))
                    .findFirst()
                    .orElseThrow(() -> AppException.notFound("User not found"));
        }
        currentUserService.requireSuperAdmin();
        return service.getById(id);
    }

    private List<UserResponse> mockUsers() {
        return List.of(
                new UserResponse(
                        1L,
                        "Josmnr",
                        "TRT9aLU2WQYXa6R3KbPzYPipm1i2",
                        "Josmnr@gmail.com",
                        true,
                        new RoleResponse(1L, CurrentUserService.ROLE_SUPER_ADMIN),
                        null
                ),
                new UserResponse(
                        2L,
                        "aleflores",
                        "AzPKc1tGz1NdK1Aj4rKX6SkBGsq1",
                        "aleflores@gmail.com",
                        true,
                        new RoleResponse(2L, CurrentUserService.ROLE_ADMIN),
                        new CompanySummary(10L, "Sanitek Solutions")
                ),
                new UserResponse(
                        3L,
                        "Maria Gonzalez",
                        "firebase_uid_user_003",
                        "maria.gonzalez@empresa.com",
                        false,
                        new RoleResponse(3L, CurrentUserService.ROLE_USER),
                        new CompanySummary(10L, "Sanitek Solutions")
                )
        );
    }
}
