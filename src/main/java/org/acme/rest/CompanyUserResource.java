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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Authenticated
@Path("/api/company-users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Company Users", description = "Administracion de usuarios de una empresa")
@SecurityRequirement(name = "firebaseAuth")
public class CompanyUserResource {

    @Inject AdminManagementService service;

    @POST
    @Operation(
            summary = "Crear usuario de empresa",
            description = "Crea un usuario regular dentro de la empresa del administrador autenticado."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Usuario creado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @APIResponse(responseCode = "400", description = "Solicitud invalida"),
            @APIResponse(responseCode = "401", description = "Token ausente o invalido"),
            @APIResponse(responseCode = "403", description = "El usuario no tiene permisos"),
            @APIResponse(responseCode = "409", description = "El correo ya esta registrado")
    })
    public Response create(
            @RequestBody(
                    description = "Datos del usuario que se agregara a la empresa del administrador",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateCompanyUserRequest.class))
            )
            @Valid CreateCompanyUserRequest request) {
        UserResponse response = service.createCompanyUser(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Operation(
            summary = "Listar usuarios de empresa",
            description = "Obtiene los usuarios regulares de la empresa del administrador autenticado."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Lista de usuarios",
                    content = @Content(schema = @Schema(
                            type = SchemaType.ARRAY,
                            implementation = UserResponse.class
                    ))
            ),
            @APIResponse(responseCode = "401", description = "Token ausente o invalido"),
            @APIResponse(responseCode = "403", description = "El usuario no tiene permisos")
    })
    public List<UserResponse> listAll() {
        return service.listCompanyUsers();
    }

    @PUT
    @Path("/{id}/deactivate")
    @Operation(
            summary = "Desactivar usuario de empresa",
            description = "Desactiva un usuario regular perteneciente a la empresa del administrador autenticado."
    )
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Usuario desactivado"),
            @APIResponse(responseCode = "400", description = "El usuario indicado no es un usuario regular"),
            @APIResponse(responseCode = "401", description = "Token ausente o invalido"),
            @APIResponse(responseCode = "403", description = "El usuario pertenece a otra empresa"),
            @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public Response deactivate(
            @Parameter(
                    name = "id",
                    description = "Identificador del usuario",
                    required = true,
                    example = "15"
            )
            @PathParam("id") Long id) {
        service.deactivateCompanyUser(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/activate")
    @Operation(
            summary = "Activar usuario de empresa",
            description = "Activa un usuario regular perteneciente a la empresa del administrador autenticado."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Usuario activado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @APIResponse(responseCode = "400", description = "El usuario indicado no es un usuario regular"),
            @APIResponse(responseCode = "401", description = "Token ausente o invalido"),
            @APIResponse(responseCode = "403", description = "El usuario pertenece a otra empresa"),
            @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public UserResponse activate(
            @Parameter(
                    name = "id",
                    description = "Identificador del usuario",
                    required = true,
                    example = "15"
            )
            @PathParam("id") Long id) {
        return service.activateCompanyUser(id);
    }
}
