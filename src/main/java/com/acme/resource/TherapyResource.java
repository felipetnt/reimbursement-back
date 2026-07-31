package com.acme.resource;

import com.acme.dto.request.create.CreateTherapyRequest;
import com.acme.dto.request.update.UpdateTherapyRequest;
import com.acme.dto.response.TherapyResponse;
import com.acme.service.TherapyService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/therapies")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(
        name = "Therapies",
        description = "Terapias dos dependentes"
)
public class TherapyResource {

    @Inject
    TherapyService service;

    @GET
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Lista as terapias da família")
    public List<TherapyResponse> list() {
        return service.list();
    }

    @GET
    @Path("/dependent/{dependentId}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Lista terapias por dependente")
    public List<TherapyResponse> listByDependent(
            @PathParam("dependentId") UUID dependentId
    ) {
        return service.listByDependent(dependentId);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Busca uma terapia pelo ID")
    public TherapyResponse findById(
            @PathParam("id") UUID id
    ) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Cria uma nova terapia")
    @APIResponse(
            responseCode = "201",
            description = "Terapia criada com sucesso"
    )
    @APIResponse(
            responseCode = "409",
            description = "Já existe uma terapia ativa equivalente"
    )
    public Response create(
            @Valid CreateTherapyRequest request
    ) {
        TherapyResponse response =
                service.create(request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Atualiza uma terapia")
    public TherapyResponse update(
            @PathParam("id") UUID id,
            @Valid UpdateTherapyRequest request
    ) {
        return service.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Remove uma terapia")
    @APIResponse(
            responseCode = "204",
            description = "Terapia removida com sucesso"
    )
    @APIResponse(
            responseCode = "409",
            description = "Terapia possui reembolsos vinculados"
    )
    public Response delete(
            @PathParam("id") UUID id
    ) {
        service.delete(id);
        return Response.noContent().build();
    }
}
