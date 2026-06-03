package org.acme.exception;

import jakarta.ws.rs.core.Response;

public class AppException extends RuntimeException {

    private final Response.Status status;

    public AppException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }

    public static AppException notFound(String message) {
        return new AppException(message, Response.Status.NOT_FOUND);
    }

    public static AppException conflict(String message) {
        return new AppException(message, Response.Status.CONFLICT);
    }

    public static AppException badRequest(String message) {
        return new AppException(message, Response.Status.BAD_REQUEST);
    }

    public static AppException unauthorized(String message) {
        return new AppException(message, Response.Status.UNAUTHORIZED);
    }

    public static AppException forbidden(String message) {
        return new AppException(message, Response.Status.FORBIDDEN);
    }

    public Response.Status getStatus() {
        return status;
    }
}
