package com.acme.resource;

import com.acme.dto.request.create.CreateReimbursementRequest;
import com.acme.dto.request.update.UpdateReimbursementRequest;
import com.acme.dto.response.ReimbursementResponse;
import com.acme.service.ReimbursementService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;
import java.util.UUID;

@Path("/reimbursements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReimbursementResource {

    @Inject
    ReimbursementService service;

    @GET
    @Operation(summary = "Lista todos os reembolsos")
    public List<ReimbursementResponse> list() {
        return service.list();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca reembolso pelo ID")
    @APIResponse(responseCode = "200", description = "Reembolso encontrado")
    @APIResponse(responseCode = "404", description = "Reembolso não encontrado")
    public ReimbursementResponse findById(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @GET
    @Path("/dependent/{dependentId}")
    @Operation(summary = "Lista reembolsos por dependente")
    @APIResponse(responseCode = "200", description = "Reembolsos encontrados")
    @APIResponse(responseCode = "404", description = "Dependente não encontrado")
    public List<ReimbursementResponse> listByDependent(@PathParam("dependentId") UUID dependentId) {
        return service.listByDependent(dependentId);
    }

    @POST
    @Operation(summary = "Cria um novo reembolso")
    @APIResponse(responseCode = "201", description = "Reembolso criado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Dependente ou tipo de terapia não encontrado")
    public Response create(@Valid CreateReimbursementRequest request) {

        ReimbursementResponse response = service.create(request);

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualiza um reembolso")
    @APIResponse(responseCode = "200", description = "Reembolso atualizado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Reembolso, dependente ou tipo de terapia não encontrado")
    public ReimbursementResponse update(@PathParam("id") UUID id,
                                        @Valid UpdateReimbursementRequest request) {
        return service.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remove um reembolso")
    @APIResponse(responseCode = "204", description = "Reembolso removido com sucesso")
    @APIResponse(responseCode = "404", description = "Reembolso não encontrado")
    public Response delete(@PathParam("id") UUID id) {

        service.delete(id);

        return Response.noContent().build();
    }
}
