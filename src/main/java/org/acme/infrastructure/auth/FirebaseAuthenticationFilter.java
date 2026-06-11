package org.acme.infrastructure.auth;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.acme.domain.AuthProvider;
import org.acme.domain.entity.User;
import org.acme.exception.AppException;
import org.acme.exception.ErrorResponse;
import org.acme.repository.UserRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Provider
@Authenticated
@Priority(Priorities.AUTHENTICATION)
public class FirebaseAuthenticationFilter implements ContainerRequestFilter {

    @Inject AuthProvider authProvider;
    @Inject UserRepository userRepository;
    @Inject AuthenticatedUser authenticatedUser;
    @ConfigProperty(name = "sanitek.mock-users.enabled", defaultValue = "false")
    boolean mockUsersEnabled;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String requestPath = requestContext.getUriInfo().getPath();
        if (requestPath.startsWith("/")) {
            requestPath = requestPath.substring(1);
        }
        if (mockUsersEnabled && (requestPath.equals("api/users") || requestPath.startsWith("api/users/"))) {
            return;
        }

        String authorization = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            abort(requestContext, Response.Status.UNAUTHORIZED, "Authorization Bearer token is required");
            return;
        }

        String token = authorization.substring("Bearer ".length()).trim();
        String firebaseUid;
        try {
            firebaseUid = authProvider.verifyToken(token);
        } catch (AppException e) {
            abort(requestContext, e.getStatus(), e.getMessage());
            return;
        }

        User user = userRepository.findAuthenticatedByFirebaseUid(firebaseUid).orElse(null);
        if (user == null) {
            abort(requestContext, Response.Status.UNAUTHORIZED, "Authenticated Firebase user is not registered");
            return;
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            abort(requestContext, Response.Status.FORBIDDEN, "User is inactive");
            return;
        }

        authenticatedUser.setUser(user);
    }

    private void abort(ContainerRequestContext requestContext, Response.Status status, String message) {
        requestContext.abortWith(Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(status.getStatusCode(), message))
                .build());
    }
}
