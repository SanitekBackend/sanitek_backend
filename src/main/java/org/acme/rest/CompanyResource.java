package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.request.CreateCompanyRequest;
import org.acme.dto.response.CompanyResponse;
import org.acme.infrastructure.auth.Authenticated;
import org.acme.service.CompanyService;

import java.util.List;

@Authenticated
@Path("/api/companies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyResource {

    @Inject CompanyService service;

    @POST
    public Response create(@Valid CreateCompanyRequest request) {
        CompanyResponse response = service.create(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    public List<CompanyResponse> listAll() {
        return service.listAll();
    }
}
