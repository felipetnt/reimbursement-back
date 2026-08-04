package com.acme.resource;

import com.acme.dto.request.create.CreateDependentRequest;
import com.acme.dto.request.update.UpdateDependentRequest;
import com.acme.dto.response.DependentDetailsResponse;
import com.acme.dto.response.DependentResponse;
import com.acme.service.DependentService;
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

@Path("/dependents")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(
        name = "Dependents",
        description = "Dependentes da família autenticada"
)
public class DependentResource {

    @Inject
    DependentService service;

    @GET
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Lista dependentes da família autenticada")
    public List<DependentResponse> list() {
        return service.list();
    }

    @GET
    @Path("/{id}/details")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(
            summary = "Obtém os detalhes, terapias e reembolsos de um dependente"
    )
    @APIResponse(
            responseCode = "200",
            description = "Detalhes encontrados"
    )
    @APIResponse(
            responseCode = "404",
            description = "Dependente não encontrado"
    )
    public DependentDetailsResponse findDetails(
            @PathParam("id") UUID id
    ) {
        return service.findDetails(id);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Busca um dependente pelo ID")
    @APIResponse(responseCode = "200", description = "Dependente encontrado")
    @APIResponse(responseCode = "404", description = "Dependente não encontrado")
    public DependentResponse findById(
            @PathParam("id") UUID id
    ) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Cria um novo dependente")
    @APIResponse(responseCode = "201", description = "Dependente criado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "403", description = "Família inválida ou usuário sem permissão")
    public Response create(
            @Valid CreateDependentRequest request
    ) {
        DependentResponse response =
                service.create(request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Atualiza um dependente")
    @APIResponse(responseCode = "200", description = "Dependente atualizado com sucesso")
    @APIResponse(responseCode = "404", description = "Dependente não encontrado")
    public DependentResponse update(@PathParam("id") UUID id, @Valid UpdateDependentRequest request) {
        return service.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Remove um dependente")
    @APIResponse(responseCode = "204", description = "Dependente removido com sucesso")
    @APIResponse(responseCode = "409", description = "Dependente possui histórico vinculado")
    public Response delete(
            @PathParam("id") UUID id
    ) {
        service.delete(id);
        return Response.noContent().build();
    }
}
