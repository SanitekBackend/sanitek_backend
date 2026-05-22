package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.response.HealthSummaryResponse;
import org.acme.service.HealthService;

import java.util.List;

@Path("/api/health")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HealthResource {

    @Inject HealthService service;

    @GET
    public List<HealthSummaryResponse> listAll() {
        return service.getAllSummaries();
    }

    @GET
    @Path("/municipality/{id}")
    public HealthSummaryResponse getByMunicipality(@PathParam("id") Long id) {
        return service.getSummaryByMunicipality(id);
    }

    @POST
    @Path("/import/{year}")
    public Response importFromCsv(@PathParam("year") int year) {
        if (year < 2000 || year > 2100) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Year must be between 2000 and 2100\"}")
                    .build();
        }
        int count = service.importFromCsv(year);
        return Response.ok("{\"imported\":" + count + ",\"year\":" + year + "}").build();
    }
}
