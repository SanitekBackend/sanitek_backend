package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.response.HealthSummaryResponse;
import org.acme.service.HealthService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/health")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Health", description = "Resumenes de salud por alcaldia")
public class HealthResource {

    @Inject HealthService service;

    @GET
    @Operation(
            summary = "Listar resumenes de salud",
            description = "Obtiene el resumen de casos de salud de todas las alcaldias."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de resumenes de salud",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = HealthSummaryResponse.class
            ))
    )
    public List<HealthSummaryResponse> listAll() {
        return service.getAllSummaries();
    }

    @GET
    @Path("/municipality/{id}")
    @Operation(
            summary = "Consultar salud por alcaldia",
            description = "Obtiene el resumen de casos de salud de una alcaldia."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Resumen de salud",
                    content = @Content(schema = @Schema(implementation = HealthSummaryResponse.class))
            ),
            @APIResponse(responseCode = "404", description = "Alcaldia no encontrada")
    })
    public HealthSummaryResponse getByMunicipality(
            @Parameter(
                    name = "id",
                    description = "Identificador de la alcaldia",
                    required = true,
                    example = "22"
            )
            @PathParam("id") Long id) {
        return service.getSummaryByMunicipality(id);
    }
}
