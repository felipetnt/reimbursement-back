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
@Tag(name = "Therapies", description = "Gerenciamento de terapias")
public class TherapyResource {

    @Inject
    TherapyService service;

    @GET
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Lista terapias", description = "O administrador lista todas. Usuários e visualizadores listam somente as terapias da própria família.")
    @APIResponse(responseCode = "200", description = "Terapias encontradas")
    public List<TherapyResponse> list() {
        return service.list();
    }

    @GET
    @Path("/dependent/{dependentId}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Lista terapias por dependente")
    @APIResponse(responseCode = "200", description = "Terapias encontradas")
    @APIResponse(responseCode = "404", description = "Dependente não encontrado")
    public List<TherapyResponse> listByDependent(@PathParam("dependentId") UUID dependentId) {
        return service.listByDependent(dependentId);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Busca uma terapia pelo ID")
    @APIResponse(responseCode = "200", description = "Terapia encontrada")
    @APIResponse(responseCode = "404", description = "Terapia não encontrada")
    public TherapyResponse findById(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Cria uma terapia", description = "O dependente e o profissional precisam pertencer à mesma família.")
    @APIResponse(responseCode = "201", description = "Terapia criada com sucesso")
    @APIResponse(responseCode = "400", description = "Datas inválidas ou entidades de famílias diferentes")
    @APIResponse(responseCode = "404", description = "Dependente ou profissional não encontrado")
    @APIResponse(responseCode = "409", description = "Já existe uma terapia ativa equivalente")
    public Response create(@Valid CreateTherapyRequest request) {
        TherapyResponse response = service.create(request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Atualiza uma terapia")
    @APIResponse(responseCode = "200", description = "Terapia atualizada com sucesso")
    @APIResponse(responseCode = "400", description = "Datas inválidas")
    @APIResponse(responseCode = "404", description = "Terapia não encontrada")
    public TherapyResponse update(@PathParam("id") UUID id, @Valid UpdateTherapyRequest request) {
        return service.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Remove uma terapia")
    @APIResponse(responseCode = "204", description = "Terapia removida com sucesso")
    @APIResponse(responseCode = "404", description = "Terapia não encontrada")
    @APIResponse(responseCode = "409", description = "Terapia possui reembolsos vinculados")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}