package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.enums.NivelRiesgo;
import org.acme.dto.request.FiltroIrsaRequest;
import org.acme.dto.response.IrsaDiagnosticoResponse;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.TendenciaIrsaResponse;
import org.acme.service.IrsaService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Path("/api/irsa")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IrsaResource {

    @Inject
    IrsaService service;

    @GET
    public List<IrsaResponse> listarUltimos() {
        return service.listarUltimos();
    }

    @GET
    @Path("/alcaldia/{id}")
    public IrsaResponse obtenerPorAlcaldia(@PathParam("id") Long id) {
        return service.obtenerUltimoPorAlcaldia(id);
    }

    @GET
    @Path("/historico")
    public List<IrsaResponse> obtenerHistorico(@BeanParam FiltroIrsaRequest filtro) {
        return service.obtenerHistorico(filtro.idAlcaldia, filtro.desde, filtro.hasta);
    }

    @GET
    @Path("/nivel/{nivel}")
    public List<IrsaResponse> listarPorNivel(@PathParam("nivel") NivelRiesgo nivel) {
        return service.listarPorNivelRiesgo(nivel);
    }

    @POST
    @Path("/calcular/{idAlcaldia}")
    public Response calcular(@PathParam("idAlcaldia") Long idAlcaldia) {
        IrsaResponse resultado = service.calcularIrsa(idAlcaldia);
        return Response.status(Response.Status.CREATED).entity(resultado).build();
    }

    @GET
    @Path("/diagnostico/{idAlcaldia}")
    public IrsaDiagnosticoResponse diagnosticar(@PathParam("idAlcaldia") Long idAlcaldia) {
        return service.diagnosticar(idAlcaldia);
    }

    @GET
    @Path("/tendencia/{idAlcaldia}")
    public TendenciaIrsaResponse tendencia(
            @PathParam("idAlcaldia") Long idAlcaldia,
            @QueryParam("periodo") @DefaultValue("SEMANAL") String periodo,
            @QueryParam("cantidad") @DefaultValue("8") int cantidad) {
        return service.obtenerTendencia(idAlcaldia, periodo, cantidad);
    }

    @GET
    @Path("/diario")
    public Response snapshotDiario(@QueryParam("fecha") String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"mensaje\":\"Parámetro 'fecha' requerido en formato YYYY-MM-DD\"}")
                    .build();
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaStr);
        } catch (DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"mensaje\":\"Formato de fecha inválido. Usa YYYY-MM-DD\"}")
                    .build();
        }
        List<IrsaResponse> resultado = service.obtenerSnapshotDiario(fecha);
        return Response.ok(resultado).build();
    }
}
