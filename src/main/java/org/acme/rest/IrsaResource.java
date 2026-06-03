package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.irsa.IrsaEngine;
import org.acme.domain.irsa.IrsaResult;
import org.acme.dto.request.IrsaBatchCalculationRequest;
import org.acme.dto.request.IrsaSimulateRequest;
import org.acme.dto.response.IrsaDiagnosticResponse;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.IrsaTrendResponse;
import org.acme.infrastructure.messaging.kafka.IrsaBatchProcessingStats;
import org.acme.infrastructure.messaging.kafka.IrsaCalculationProducer;
import org.acme.service.IrsaService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Path("/api/irsa")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IrsaResource {

    @Inject
    IrsaService service;

    @Inject
    IrsaCalculationProducer irsaCalculationProducer;

    @Inject
    IrsaBatchProcessingStats irsaBatchProcessingStats;

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

    @POST
    @Path("/calculate/{municipalityId}/async")
    public Response calculateAsync(@PathParam("municipalityId") Long municipalityId) {
        String batchId = irsaCalculationProducer.publishSingle(municipalityId);
        return Response.accepted(Map.of("batchId", batchId, "enqueued", 1)).build();
    }

    @POST
    @Path("/batch/calculate")
    public Response calculateBatch(@Valid IrsaBatchCalculationRequest request) {
        String batchId = irsaCalculationProducer.publishBatch(request.municipalityIds());
        return Response.accepted(Map.of("batchId", batchId, "enqueued", request.municipalityIds().size())).build();
    }

    @POST
    @Path("/batch/calculate/{municipalityIds}")
    public Response calculateBatchFromPath(@PathParam("municipalityIds") String municipalityIds) {
        List<Long> ids = Arrays.stream(municipalityIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(Long::valueOf)
                .toList();

        String batchId = irsaCalculationProducer.publishBatch(ids);
        return Response.accepted(Map.of("batchId", batchId, "enqueued", ids.size())).build();
    }

    @GET
    @Path("/batch/status/{batchId}")
    public Response batchStatus(@PathParam("batchId") String batchId) {
        IrsaBatchProcessingStats.Snapshot snapshot = irsaBatchProcessingStats.snapshot(batchId);
        if (snapshot == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", "Batch not found", "batchId", batchId))
                    .build();
        }
        return Response.ok(snapshot).build();
    }

    @GET
    @Path("/batch/status")
    public Map<String, IrsaBatchProcessingStats.Snapshot> allBatchStatuses() {
        return irsaBatchProcessingStats.allSnapshots();
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
