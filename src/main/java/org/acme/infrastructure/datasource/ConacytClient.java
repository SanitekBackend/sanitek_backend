package org.acme.infrastructure.datasource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "conacyt-api")
public interface ConacytClient {

    @GET
    @Path("/datos")
    String getDatos(@QueryParam("municipio") String municipio);
}
