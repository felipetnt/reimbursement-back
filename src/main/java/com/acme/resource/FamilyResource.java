package com.acme.resource;

import com.acme.dto.request.create.CreateFamilyRequest;
import com.acme.dto.request.update.UpdateFamilyRequest;
import com.acme.dto.response.FamilyResponse;
import com.acme.service.FamilyService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/families")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Families", description = "Gestao de Familias")
public class FamilyResource {

    @Inject
    private FamilyService service;

    @GET
    @Operation(summary = "Lista todas as familias")
    public List<FamilyResponse> list(){
        return service.readAll();
    }

    @GET
    @Path("/{familyId}")
    @Operation(summary = "Obtem detalhes de uma familia")
    @APIResponse(responseCode = "200", description = "Familia encontrada")
    @APIResponse(responseCode = "404", description = "Familia nao encontrada")
    public FamilyResponse getById(@PathParam("familyId") UUID id){
        return service.getFamilyById(id);
    }

    @POST
    @Operation(summary = "Cria uma nova família")
    @APIResponse(responseCode = "201", description = "Família criada com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    public Response create(@Valid CreateFamilyRequest dto){
        FamilyResponse response = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{familyId}")
    @Operation(summary = "Atualiza uma familia pelo seu id")
    @APIResponse(responseCode = "200", description = "Atualizacao feita com sucesso")
    @APIResponse(responseCode = "400", description = "Dado nulo ou invalido")
    @APIResponse(responseCode = "404", description = "Familia nao encontrada")
    @APIResponse(responseCode = "409", description = "Conflito ao atualizar familia")
    public FamilyResponse update(@PathParam("familyId") UUID id, @Valid UpdateFamilyRequest dto){
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{familyId}")
    @Operation(summary = "Deleta uma familia pelo seu id")
    @APIResponse(responseCode = "200", description = "Delete feito com sucesso")
    @APIResponse(responseCode = "400", description = "Dado nulo ou invalido")
    @APIResponse(responseCode = "404", description = "Familia nao encontrada")
    public Response delete(@PathParam("familyId") UUID id){
        service.delete(id);
        return Response.noContent().build();
    }






}

