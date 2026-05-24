package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.irsa.IrsaEngine;
import org.acme.domain.irsa.IrsaResult;
import org.acme.dto.request.IrsaSimulateRequest;
import org.acme.dto.response.IrsaDiagnosticResponse;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.IrsaTrendResponse;
import org.acme.service.IrsaService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Path("/api/irsa")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IrsaResource {

    @Inject
    IrsaService service;

    private final IrsaEngine engine = new IrsaEngine();

    @GET
    public List<IrsaResponse> listLatest() {
        return service.listLatestAll();
    }

    @GET
    @Path("/municipality/{id}")
    public IrsaResponse getByMunicipality(@PathParam("id") Long id) {
        return service.getLatestByMunicipality(id);
    }

    @GET
    @Path("/history")
    public List<IrsaResponse> getHistorical(
            @QueryParam("municipalityId") Long municipalityId,
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr
    ) {

        Instant from = fromStr != null
                ? Instant.parse(fromStr)
                : Instant.now().minusSeconds(60L * 60L * 24L * 30L);

        Instant to = toStr != null
                ? Instant.parse(toStr)
                : Instant.now();

        return service.getHistorical(municipalityId, from, to);
    }

    @GET
    @Path("/risk-level/{level}")
    public List<IrsaResponse> listByRiskLevel(@PathParam("level") String level) {
        return service.listByRiskLevel(level);
    }

    @POST
    @Path("/calculate/{municipalityId}")
    public Response calculate(@PathParam("municipalityId") Long municipalityId) {

        IrsaResponse result = service.calculate(municipalityId);

        return Response.status(Response.Status.CREATED)
                .entity(result)
                .build();
    }

    @GET
    @Path("/diagnostic/{municipalityId}")
    public IrsaDiagnosticResponse getDiagnostic(
            @PathParam("municipalityId") Long municipalityId
    ) {
        return service.getDiagnostic(municipalityId);
    }

    @GET
    @Path("/trend/{municipalityId}")
    public IrsaTrendResponse getTrend(
            @PathParam("municipalityId") Long municipalityId,

            @QueryParam("period")
            @DefaultValue("WEEKLY")
            String period,

            @QueryParam("count")
            @DefaultValue("8")
            int count
    ) {

        return service.getTrend(municipalityId, period, count);
    }

    @GET
    @Path("/daily")
    public Response getDailySnapshot(
            @QueryParam("date") String dateStr
    ) {

        if (dateStr == null || dateStr.isBlank()) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("""
                        {
                          "message": "Parameter 'date' is required in YYYY-MM-DD format"
                        }
                        """)
                    .build();
        }

        LocalDate date;

        try {

            date = LocalDate.parse(dateStr);

        } catch (DateTimeParseException e) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("""
                        {
                          "message": "Invalid date format. Use YYYY-MM-DD"
                        }
                        """)
                    .build();
        }

        List<IrsaResponse> result = service.getDailySnapshot(date);

        return Response.ok(result).build();
    }

    @POST
    @Path("/simulate")
    public IrsaResult simulate(@Valid IrsaSimulateRequest req) {
        return engine.calculate(
                req.avgNo2(),
                req.avgO3(),
                req.avgPm25(),
                req.avgUv(),
                req.avgTmp(),
                req.copdCount(),
                req.asthmaCount(),
                req.pneumoniaCount(),
                req.smokingCount()
        );
    }
}