package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.response.MeasurementResponse;
import org.acme.service.PollutantService;

import java.time.Instant;
import java.util.List;

@Path("/api/measurements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MeasurementResource {

    @Inject PollutantService service;

    @GET
    @Path("/no2")
    public List<MeasurementResponse> getNo2(
            @QueryParam("stationId") Long stationId,
            @QueryParam("from") Instant from,
            @QueryParam("to") Instant to) {
        if (stationId == null || from == null || to == null) {
            throw new WebApplicationException("Parameters stationId, from, and to are required", 400);
        }
        return service.getNo2ByStation(stationId, from, to);
    }

    @GET
    @Path("/o3")
    public List<MeasurementResponse> getO3(
            @QueryParam("stationId") Long stationId,
            @QueryParam("from") Instant from,
            @QueryParam("to") Instant to) {
        if (stationId == null || from == null || to == null) {
            throw new WebApplicationException("Parameters stationId, from, and to are required", 400);
        }
        return service.getO3ByStation(stationId, from, to);
    }

    @GET
    @Path("/pm25")
    public List<MeasurementResponse> getPm25(
            @QueryParam("stationId") Long stationId,
            @QueryParam("from") Instant from,
            @QueryParam("to") Instant to) {
        if (stationId == null || from == null || to == null) {
            throw new WebApplicationException("Parameters stationId, from, and to are required", 400);
        }
        return service.getPm25ByStation(stationId, from, to);
    }
}
