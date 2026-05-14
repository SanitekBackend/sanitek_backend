package org.acme.infrastructure.datasource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "inegi-api")
public interface InegiClient {

    @GET
    @Path("/indicadores")
    String getIndicadores(@QueryParam("indicadores") String claves, @QueryParam("token") String token);
}
