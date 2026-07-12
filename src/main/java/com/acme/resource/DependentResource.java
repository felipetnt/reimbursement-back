package com.acme.resource;

import com.acme.dto.request.create.CreateDependentRequest;
import com.acme.dto.request.update.UpdateDependentRequest;
import com.acme.dto.response.DependentResponse;
import com.acme.service.DependentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;
import java.util.UUID;

@Path("/dependents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DependentResource {

    @Inject
    DependentService service;

    @GET
    @Operation(summary = "Lista todos os dependentes")
    public List<DependentResponse> list() {
        return service.list();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca dependente pelo ID")
    @APIResponse(responseCode = "200", description = "Dependente encontrado")
    @APIResponse(responseCode = "404", description = "Dependente não encontrado")
    public DependentResponse findById(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @GET
    @Path("/family/{familyId}")
    @Operation(summary = "Lista dependentes por família")
    @APIResponse(responseCode = "200", description = "Dependentes encontrados")
    @APIResponse(responseCode = "404", description = "Família não encontrada")
    public List<DependentResponse> listByFamily(@PathParam("familyId") UUID familyId) {
        return service.listByFamily(familyId);
    }

    @POST
    @Operation(summary = "Cria um novo dependente")
    @APIResponse(responseCode = "201", description = "Dependente criado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Família não encontrada")
    public Response create(@Valid CreateDependentRequest request) {

        DependentResponse response = service.create(request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualiza um dependente")
    @APIResponse(responseCode = "200", description = "Dependente atualizado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Dependente ou família não encontrada")
    public DependentResponse update(@PathParam("id") UUID id,
                                    @Valid UpdateDependentRequest request) {
        return service.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remove um dependente")
    @APIResponse(responseCode = "204", description = "Dependente removido com sucesso")
    @APIResponse(responseCode = "404", description = "Dependente não encontrado")
    public Response delete(@PathParam("id") UUID id) {

        service.delete(id);

        return Response.noContent().build();
    }
}