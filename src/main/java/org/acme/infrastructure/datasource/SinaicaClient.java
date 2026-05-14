package org.acme.infrastructure.datasource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "sinaica-api")
public interface SinaicaClient {

    @GET
    @Path("/estaciones")
    String getEstaciones(@QueryParam("red") String red);
}
