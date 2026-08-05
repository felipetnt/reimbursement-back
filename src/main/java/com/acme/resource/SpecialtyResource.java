package com.acme.resource;

import com.acme.dto.request.create.CreateSpecialtyRequest;
import com.acme.dto.request.update.UpdateSpecialtyRequest;
import com.acme.dto.response.SpecialtyResponse;
import com.acme.service.SpecialtyService;
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

@Path("/specialties")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Specialties", description = "Gerenciamento de especialidades")
public class SpecialtyResource {

    @Inject
    SpecialtyService service;

    @GET
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Lista especialidades", description = "O administrador lista todas. Usuários e visualizadores listam somente as especialidades da própria família.")
    public List<SpecialtyResponse> list() {
        return service.list();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Busca uma especialidade pelo ID")
    @APIResponse(responseCode = "200", description = "Especialidade encontrada")
    @APIResponse(responseCode = "404", description = "Especialidade não encontrada")
    public SpecialtyResponse findById(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Cria uma especialidade", description = "O administrador cria em qualquer família. Um usuário cria somente na própria família.")
    @APIResponse(responseCode = "201", description = "Especialidade criada com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "403", description = "Família inválida ou usuário sem permissão")
    @APIResponse(responseCode = "409", description = "Especialidade duplicada")
    public Response create(@Valid CreateSpecialtyRequest request) {
        SpecialtyResponse response = service.create(request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Atualiza uma especialidade")
    @APIResponse(responseCode = "200", description = "Especialidade atualizada com sucesso")
    @APIResponse(responseCode = "404", description = "Especialidade não encontrada")
    @APIResponse(responseCode = "409", description = "Especialidade duplicada")
    public SpecialtyResponse update(@PathParam("id") UUID id, @Valid UpdateSpecialtyRequest request) {
        return service.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Remove uma especialidade")
    @APIResponse(responseCode = "204", description = "Especialidade removida com sucesso")
    @APIResponse(responseCode = "404", description = "Especialidade não encontrada")
    @APIResponse(responseCode = "409", description = "Especialidade possui profissionais vinculados")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}