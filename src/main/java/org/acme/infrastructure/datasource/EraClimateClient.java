package org.acme.infrastructure.datasource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "era-climate-api")
public interface EraClimateClient {

    @GET
    @Path("/resources")
    String getClimateData(@QueryParam("variable") String variable, @QueryParam("area") String area);
}
