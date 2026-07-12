package com.acme.resource;

import com.acme.dto.request.create.CreateTherapyTypeRequest;
import com.acme.dto.request.update.UpdateTherapyTypeRequest;
import com.acme.dto.response.TherapyTypeResponse;
import com.acme.service.TherapyTypeService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;
import java.util.UUID;

@Path("/therapy-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TherapyTypeResource {

    @Inject
    TherapyTypeService service;

    @GET
    @Operation(summary = "Lista todos os tipos de terapia")
    public List<TherapyTypeResponse> list() {
        return service.list();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca tipo de terapia pelo ID")
    @APIResponse(responseCode = "200", description = "Tipo de terapia encontrado")
    @APIResponse(responseCode = "404", description = "Tipo de terapia não encontrado")
    public TherapyTypeResponse findById(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @GET
    @Path("/dependent/{dependentId}")
    @Operation(summary = "Lista tipos de terapia por dependente")
    @APIResponse(responseCode = "200", description = "Tipos de terapia encontrados")
    @APIResponse(responseCode = "404", description = "Dependente não encontrado")
    public List<TherapyTypeResponse> listByDependent(@PathParam("dependentId") UUID dependentId) {
        return service.listByDependent(dependentId);
    }

    @POST
    @Operation(summary = "Cria um novo tipo de terapia")
    @APIResponse(responseCode = "201", description = "Tipo de terapia criado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Dependente não encontrado")
    @APIResponse(responseCode = "409", description = "Tipo de terapia já cadastrado para este dependente")
    public Response create(@Valid CreateTherapyTypeRequest request) {
        TherapyTypeResponse response = service.create(request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualiza um tipo de terapia")
    @APIResponse(responseCode = "200", description = "Tipo de terapia atualizado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Tipo de terapia ou dependente não encontrado")
    @APIResponse(responseCode = "409", description = "Tipo de terapia já cadastrado para este dependente")
    public TherapyTypeResponse update(@PathParam("id") UUID id,
                                      @Valid UpdateTherapyTypeRequest request) {
        return service.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remove um tipo de terapia")
    @APIResponse(responseCode = "204", description = "Tipo de terapia removido com sucesso")
    @APIResponse(responseCode = "404", description = "Tipo de terapia não encontrado")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);

        return Response.noContent().build();
    }
}