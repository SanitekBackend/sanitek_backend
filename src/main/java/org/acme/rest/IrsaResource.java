package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.response.IrsaBackfillResponse;
import org.acme.dto.response.IrsaDiagnosticResponse;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.IrsaTimelineSnapshotResponse;
import org.acme.dto.response.IrsaTrendResponse;
import org.acme.infrastructure.messaging.kafka.IrsaBatchProcessingStats;
import org.acme.service.AlertEmailService;
import org.acme.service.IrsaService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Path("/api/irsa")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IrsaResource {

    @Inject
    IrsaService service;

    @Inject
    IrsaBatchProcessingStats irsaBatchProcessingStats;

    @Inject
    AlertEmailService alertEmailService;

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
        if (isHighRisk(result.riskLevel())) {
            alertEmailService.sendRiskDetected(result);
        }

        return Response.status(Response.Status.CREATED)
                .entity(result)
                .build();
    }

    @POST
    @Path("/calculate/{municipalityId}/async")
    public Response calculateAsync(@PathParam("municipalityId") Long municipalityId) {
        return messagingUnavailable();
    }

    @POST
    @Path("/batch/calculate")
    public Response calculateBatch() {
        return messagingUnavailable();
    }

    @POST
    @Path("/batch/calculate/{municipalityIds}")
    public Response calculateBatchFromPath(@PathParam("municipalityIds") String municipalityIds) {
        return messagingUnavailable();
    }

    @POST
    @Path("/batch/calculate/all")
    public Response calculateAllAsync() {
        return messagingUnavailable();
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

    private Response messagingUnavailable() {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(Map.of("message", "Asynchronous messaging is not enabled"))
                .build();
    }

    private boolean isHighRisk(String riskLevel) {
        return "HIGH".equalsIgnoreCase(riskLevel) || "CRITICAL".equalsIgnoreCase(riskLevel);
    }

    @GET
    @Path("/diagnostic/{municipalityId}")
    public IrsaDiagnosticResponse getDiagnostic(
            @PathParam("municipalityId") Long municipalityId
    ) {
        return service.getDiagnostic(municipalityId);
    }

    @GET
    @Path("/diagnostic/timeline")
    public IrsaTimelineSnapshotResponse getDiagnosticTimelineAll(
            @QueryParam("offsetDays") @DefaultValue("0") int offsetDays
    ) {
        return service.getTimelineSnapshot(offsetDays);
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

    @POST
    @Path("/backfill/monthly")
    @Consumes(MediaType.WILDCARD)
    public Response backfillMonthly(
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr
    ) {
        if (fromStr == null || fromStr.isBlank() || toStr == null || toStr.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Query params 'from' and 'to' are required in YYYY-MM-DD format"))
                    .build();
        }

        try {
            LocalDate from = LocalDate.parse(fromStr);
            LocalDate to = LocalDate.parse(toStr);
            IrsaBackfillResponse result = service.backfillMonthly(from, to);
            return Response.ok(result).build();
        } catch (DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Invalid date format. Use YYYY-MM-DD"))
                    .build();
        }
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
    
}
