package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.domain.enums.NivelRezago;
import org.acme.dto.response.AlcaldiaResponse;
import org.acme.service.AlcaldiaService;

import java.util.List;

@Path("/api/alcaldias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlcaldiaResource {

    @Inject
    AlcaldiaService service;

    @GET
    public List<AlcaldiaResponse> listarTodas(
            @QueryParam("nivelRezago") NivelRezago nivelRezago) {
        if (nivelRezago != null) {
            return service.filtrarPorNivelRezago(nivelRezago);
        }
        return service.obtenerTodas();
    }

    @GET
    @Path("/{id}")
    public AlcaldiaResponse obtenerPorId(@PathParam("id") Long id) {
        return service.obtenerPorId(id);
    }
}
