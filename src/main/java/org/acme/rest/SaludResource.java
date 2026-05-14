package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.response.SaludResponse;
import org.acme.service.SaludService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Path("/api/salud")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Salud", description = "Datos epidemiológicos agregados por alcaldía")
public class SaludResource {

    @Inject SaludService saludService;

    @POST
    @Path("/importar")
    @Operation(summary = "Importar datos de salud desde CSVs",
               description = "Lee los archivos CSV de docs/output_by_year y agrega los datos por alcaldía y mes. "
                           + "Si ya existen datos para ese año los reemplaza.")
    public Response importar(@QueryParam("anio") int anio) {
        if (anio < 2020 || anio > 2030) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("mensaje", "Año inválido: " + anio))
                    .build();
        }
        int registros = saludService.importarDatos(anio);
        return Response.ok(Map.of(
                "mensaje", "Importación completada",
                "anio", anio,
                "registrosPersistidos", registros
        )).build();
    }

    @GET
    @Path("/alcaldia/{id}")
    @Operation(summary = "Datos de salud históricos por alcaldía")
    public List<SaludResponse> porAlcaldia(@PathParam("id") Long idAlcaldia) {
        return saludService.listarPorAlcaldia(idAlcaldia);
    }

    @GET
    @Path("/anio/{anio}")
    @Operation(summary = "Datos de salud de todas las alcaldías para un año")
    public List<SaludResponse> porAnio(@PathParam("anio") int anio) {
        return saludService.listarPorAnio(anio);
    }
}
