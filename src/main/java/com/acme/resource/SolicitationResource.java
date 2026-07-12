package com.acme.resource;

import com.acme.dto.request.create.CreateSolicitationRequest;
import com.acme.dto.request.update.UpdateSolicitationRequest;
import com.acme.dto.response.SolicitationResponse;
import com.acme.service.SolicitationService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;
import java.util.UUID;

@Path("/solicitations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SolicitationResource {

    @Inject
    SolicitationService service;

    @GET
    @Operation(summary = "Lista todas as solicitações")
    public List<SolicitationResponse> list() {
        return service.list();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca solicitação pelo ID")
    @APIResponse(responseCode = "200", description = "Solicitação encontrada")
    @APIResponse(responseCode = "404", description = "Solicitação não encontrada")
    public SolicitationResponse findById(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @GET
    @Path("/reimbursement/{reimbursementId}")
    @Operation(summary = "Lista solicitações por reembolso")
    @APIResponse(responseCode = "200", description = "Solicitações encontradas")
    @APIResponse(responseCode = "404", description = "Reembolso não encontrado")
    public List<SolicitationResponse> listByReimbursement(
            @PathParam("reimbursementId") UUID reimbursementId
    ) {
        return service.listByReimbursement(reimbursementId);
    }

    @POST
    @Operation(summary = "Cria uma nova solicitação")
    @APIResponse(responseCode = "201", description = "Solicitação criada com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Reembolso não encontrado")
    @APIResponse(responseCode = "409", description = "Conflito ao criar solicitação")
    public Response create(@Valid CreateSolicitationRequest request) {

        SolicitationResponse response = service.create(request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualiza uma solicitação")
    @APIResponse(responseCode = "200", description = "Solicitação atualizada com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Solicitação não encontrada")
    @APIResponse(responseCode = "409", description = "Conflito ao atualizar solicitação")
    public SolicitationResponse update(@PathParam("id") UUID id,
                                       @Valid UpdateSolicitationRequest request) {
        return service.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remove uma solicitação")
    @APIResponse(responseCode = "204", description = "Solicitação removida com sucesso")
    @APIResponse(responseCode = "404", description = "Solicitação não encontrada")
    public Response delete(@PathParam("id") UUID id) {

        service.delete(id);

        return Response.noContent().build();
    }

}
