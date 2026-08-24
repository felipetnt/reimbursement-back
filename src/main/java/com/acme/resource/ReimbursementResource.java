package com.acme.resource;

import com.acme.dto.request.create.CreateReimbursementRequest;
import com.acme.dto.request.update.UpdateReimbursementRequest;
import com.acme.dto.response.ReimbursementDetailsResponse;
import com.acme.dto.response.ReimbursementResponse;
import com.acme.service.ReimbursementService;
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

@Path("/reimbursements")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(
        name = "Reimbursements",
        description = "Gerenciamento de reembolsos"
)
public class ReimbursementResource {

    @Inject
    ReimbursementService service;

    @GET
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(
            summary = "Lista reembolsos",
            description = "O administrador lista todos. Usuários e visualizadores listam somente os reembolsos da própria família."
    )
    @APIResponse(responseCode = "200", description = "Reembolsos encontrados")
    public List<ReimbursementResponse> list() {
        return service.list();
    }

    @GET
    @Path("/dependent/{dependentId}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Lista reembolsos por dependente")
    @APIResponse(responseCode = "200", description = "Reembolsos encontrados")
    @APIResponse(responseCode = "404", description = "Dependente não encontrado")
    public List<ReimbursementResponse> listByDependent(@PathParam("dependentId") UUID dependentId) {
        return service.listByDependent(dependentId);
    }

    @GET
    @Path("/{id}/details")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Obtém o reembolso e seu histórico de solicitações")
    @APIResponse(responseCode = "200", description = "Detalhes encontrados")
    @APIResponse(responseCode = "404", description = "Reembolso não encontrado")
    public ReimbursementDetailsResponse findDetails(@PathParam("id") UUID id) {
        return service.findDetails(id);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Busca um reembolso pelo ID")
    @APIResponse(responseCode = "200", description = "Reembolso encontrado")
    @APIResponse(responseCode = "404", description = "Reembolso não encontrado")
    public ReimbursementResponse findById(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Cria um novo reembolso")
    @APIResponse(responseCode = "201", description = "Reembolso criado com sucesso")
    @APIResponse(responseCode = "400", description = "Mês de referência inválido")
    @APIResponse(responseCode = "404", description = "Terapia não encontrada")
    @APIResponse(responseCode = "409", description = "Reembolso possui histórico de solicitação ou documentos vinculados")
    public Response create(@Valid CreateReimbursementRequest request) {
        ReimbursementResponse response = service.create(request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Atualiza um reembolso ainda não enviado")
    @APIResponse(responseCode = "200", description = "Reembolso atualizado com sucesso")
    @APIResponse(responseCode = "400", description = "Mês de referência inválido")
    @APIResponse(responseCode = "404", description = "Reembolso não encontrado")
    @APIResponse(responseCode = "409", description = "Reembolso já enviado ou mês duplicado")
    public ReimbursementResponse update(
            @PathParam("id") UUID id,
            @Valid UpdateReimbursementRequest request
    ) {
        return service.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Remove um reembolso ainda não enviado")
    @APIResponse(responseCode = "204", description = "Reembolso removido com sucesso")
    @APIResponse(responseCode = "404", description = "Reembolso não encontrado")
    @APIResponse(responseCode = "409", description = "Reembolso já possui histórico enviado")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}