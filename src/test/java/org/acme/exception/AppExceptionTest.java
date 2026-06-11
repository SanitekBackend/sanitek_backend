package org.acme.exception;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AppException — factory methods y status HTTP")
class AppExceptionTest {

    @Test
    @DisplayName("notFound() → HTTP 404 con mensaje correcto")
    void notFound_status404() {
        AppException ex = AppException.notFound("Municipality not found");

        assertEquals("Municipality not found", ex.getMessage());
        assertEquals(Response.Status.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("conflict() → HTTP 409 con mensaje correcto")
    void conflict_status409() {
        AppException ex = AppException.conflict("User already exists");

        assertEquals("User already exists", ex.getMessage());
        assertEquals(Response.Status.CONFLICT, ex.getStatus());
    }

    @Test
    @DisplayName("badRequest() → HTTP 400 con mensaje correcto")
    void badRequest_status400() {
        AppException ex = AppException.badRequest("Invalid date format");

        assertEquals("Invalid date format", ex.getMessage());
        assertEquals(Response.Status.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("unauthorized() → HTTP 401 con mensaje correcto")
    void unauthorized_status401() {
        AppException ex = AppException.unauthorized("Token inválido");

        assertEquals("Token inválido", ex.getMessage());
        assertEquals(Response.Status.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    @DisplayName("forbidden() → HTTP 403 con mensaje correcto")
    void forbidden_status403() {
        AppException ex = AppException.forbidden("Acceso denegado");

        assertEquals("Acceso denegado", ex.getMessage());
        assertEquals(Response.Status.FORBIDDEN, ex.getStatus());
    }

    @Test
    @DisplayName("AppException es subclase de RuntimeException (no checked)")
    void esRuntimeException() {
        AppException ex = AppException.notFound("test");

        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("getStatus() devuelve el mismo status con el que fue construida")
    void getStatus_devuelveStatusOriginal() {
        AppException ex = new AppException("msg", Response.Status.SERVICE_UNAVAILABLE);

        assertEquals(Response.Status.SERVICE_UNAVAILABLE, ex.getStatus());
    }

    @Test
    @DisplayName("mensaje en notFound() puede ser cualquier texto descriptivo")
    void notFound_mensajeArbitrario() {
        String mensaje = "Station con id=99 no existe";
        AppException ex = AppException.notFound(mensaje);

        assertEquals(mensaje, ex.getMessage());
    }
}
