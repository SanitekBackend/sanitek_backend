package org.acme.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<AppException> {

    @Override
    public Response toResponse(AppException e) {
        return Response
                .status(e.getStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(e.getStatus().getStatusCode(), e.getMessage()))
                .build();
    }
}
