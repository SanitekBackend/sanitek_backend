package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.request.CreateCompanyRequest;
import org.acme.dto.response.CompanyResponse;
import org.acme.infrastructure.auth.Authenticated;
import org.acme.service.CompanyService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Authenticated
@Path("/api/companies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Companies", description = "Administracion de empresas")
@SecurityRequirement(name = "firebaseAuth")
public class CompanyResource {

    @Inject CompanyService service;

    @POST
    @Operation(
            summary = "Crear empresa",
            description = "Crea una empresa activa. Requiere un usuario con rol SUPER_ADMIN."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Empresa creada",
                    content = @Content(schema = @Schema(implementation = CompanyResponse.class))
            ),
            @APIResponse(responseCode = "400", description = "Solicitud invalida"),
            @APIResponse(responseCode = "401", description = "Token ausente o invalido"),
            @APIResponse(responseCode = "403", description = "El usuario no tiene permisos"),
            @APIResponse(responseCode = "409", description = "La empresa ya existe")
    })
    public Response create(@Valid CreateCompanyRequest request) {
        CompanyResponse response = service.create(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Operation(
            summary = "Listar empresas",
            description = "Obtiene todas las empresas. Requiere un usuario con rol SUPER_ADMIN."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Lista de empresas",
                    content = @Content(schema = @Schema(
                            type = SchemaType.ARRAY,
                            implementation = CompanyResponse.class
                    ))
            ),
            @APIResponse(responseCode = "401", description = "Token ausente o invalido"),
            @APIResponse(responseCode = "403", description = "El usuario no tiene permisos")
    })
    public List<CompanyResponse> listAll() {
        return service.listAll();
    }
}
