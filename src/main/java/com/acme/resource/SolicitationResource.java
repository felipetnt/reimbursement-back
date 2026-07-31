package com.acme.resource;

import com.acme.dto.request.create.CreateSolicitationRequest;
import com.acme.dto.request.update.UpdateSolicitationRequest;
import com.acme.dto.response.SolicitationResponse;
import com.acme.service.SolicitationService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
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

@Path("/solicitations")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(
        name = "Solicitations",
        description = "Tentativas de solicitação dos reembolsos"
)
public class SolicitationResource {

    @Inject
    SolicitationService service;

    @GET
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Lista as solicitações da família")
    public List<SolicitationResponse> list() {
        return service.list();
    }

    @GET
    @Path("/reimbursement/{reimbursementId}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Lista solicitações por reembolso")
    public List<SolicitationResponse> listByReimbursement(
            @PathParam("reimbursementId")
            UUID reimbursementId
    ) {
        return service.listByReimbursement(
                reimbursementId
        );
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Busca uma solicitação pelo ID")
    public SolicitationResponse findById(
            @PathParam("id") UUID id
    ) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(
            summary = "Cria uma nova tentativa após uma solicitação negada"
    )
    @APIResponse(
            responseCode = "201",
            description = "Nova tentativa criada com sucesso"
    )
    @APIResponse(
            responseCode = "409",
            description = "A última solicitação ainda não foi negada"
    )
    public Response create(
            @Valid CreateSolicitationRequest request
    ) {
        SolicitationResponse response =
                service.create(request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Atualiza o status de uma solicitação")
    @APIResponse(
            responseCode = "200",
            description = "Solicitação atualizada com sucesso"
    )
    @APIResponse(
            responseCode = "400",
            description = "Transição de status inválida"
    )
    public SolicitationResponse update(
            @PathParam("id") UUID id,
            @Valid UpdateSolicitationRequest request
    ) {
        return service.update(id, request);
    }
}
