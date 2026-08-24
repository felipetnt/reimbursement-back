package com.acme.resource;

import com.acme.dto.request.create.CreateFamilyRequest;
import com.acme.dto.request.update.UpdateFamilyRequest;
import com.acme.dto.response.FamilyResponse;
import com.acme.dto.response.MyFamilyResponse;
import com.acme.service.FamilyService;
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

@Path("/families")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Families", description = "Gerenciamento de famílias")
public class FamilyResource {

    @Inject
    FamilyService service;

    @GET
    @RolesAllowed("ADMIN")
    @Operation(summary = "Lista todas as famílias")
    @APIResponse(responseCode = "200", description = "Famílias encontradas")
    public List<FamilyResponse> list() {
        return service.list();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Busca uma família pelo ID")
    @APIResponse(responseCode = "200", description = "Família encontrada")
    @APIResponse(responseCode = "404", description = "Família não encontrada")
    public FamilyResponse findById(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed("ADMIN")
    @Operation(summary = "Cria uma nova família")
    @APIResponse(responseCode = "201", description = "Família criada com sucesso")
    @APIResponse(responseCode = "409", description = "Já existe uma família com esse nome")
    public Response create(@Valid CreateFamilyRequest request) {
        FamilyResponse response = service.create(request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Atualiza uma família")
    @APIResponse(responseCode = "200", description = "Família atualizada com sucesso")
    @APIResponse(responseCode = "404", description = "Família não encontrada")
    public FamilyResponse update(@PathParam("id") UUID id, @Valid UpdateFamilyRequest request) {
        return service.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Remove uma família")
    @APIResponse(responseCode = "204", description = "Família removida com sucesso")
    @APIResponse(responseCode = "404", description = "Família não encontrada")
    @APIResponse(responseCode = "409", description = "Família possui dados vinculados")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);

        return Response.noContent().build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({"USER", "VIEWER"})
    @Operation(summary = "Obtém a família do usuário autenticado")
    @APIResponse(responseCode = "200", description = "Família encontrada")
    public FamilyResponse getMyFamily() {
        return service.getMyFamily();
    }

    @GET
    @Path("/me/details")
    @RolesAllowed({"USER", "VIEWER"})
    @Operation(summary = "Obtém os detalhes da família autenticada")
    @APIResponse(responseCode = "200", description = "Detalhes encontrados")
    public MyFamilyResponse getMyFamilyDetails() {
        return service.getMyFamilyDetails();
    }

    @PUT
    @Path("/me")
    @RolesAllowed("USER")
    @Operation(summary = "Atualiza a própria família")
    @APIResponse(responseCode = "200", description = "Família atualizada com sucesso")
    @APIResponse(responseCode = "403", description = "Usuário sem permissão")
    public FamilyResponse updateMyFamily(@Valid UpdateFamilyRequest request) {
        return service.updateMyFamily(request);
    }
}