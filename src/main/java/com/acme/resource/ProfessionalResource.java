package com.acme.resource;

import com.acme.dto.request.create.CreateProfessionalRequest;
import com.acme.dto.request.update.UpdateProfessionalRequest;
import com.acme.dto.response.ProfessionalResponse;
import com.acme.service.ProfessionalService;
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

@Path("/professionals")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(
        name = "Professionals",
        description = "Gerenciamento de profissionais"
)
public class ProfessionalResource {

    @Inject
    ProfessionalService service;

    @GET
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(
            summary = "Lista profissionais",
            description = "O administrador lista todos. Usuários e visualizadores listam somente os profissionais da própria família."
    )
    public List<ProfessionalResponse> list() {
        return service.list();
    }

    @GET
    @Path("/specialty/{specialtyId}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Lista profissionais por especialidade")
    @APIResponse(responseCode = "200", description = "Profissionais encontrados")
    @APIResponse(responseCode = "404", description = "Especialidade não encontrada")
    public List<ProfessionalResponse> listBySpecialty(@PathParam("specialtyId") UUID specialtyId) {
        return service.listBySpecialty(specialtyId);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Busca um profissional pelo ID")
    @APIResponse(responseCode = "200", description = "Profissional encontrado")
    @APIResponse(responseCode = "404", description = "Profissional não encontrado")
    public ProfessionalResponse findById(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(
            summary = "Cria um profissional",
            description = "O administrador cria em qualquer família. Um usuário cria somente na própria família."
    )
    @APIResponse(responseCode = "201", description = "Profissional criado com sucesso")
    @APIResponse(responseCode = "400", description = "Família e especialidade incompatíveis ou dados inválidos")
    @APIResponse(responseCode = "403", description = "Família inválida ou usuário sem permissão")
    @APIResponse(responseCode = "404", description = "Família ou especialidade não encontrada")
    public Response create(@Valid CreateProfessionalRequest request) {
        ProfessionalResponse response = service.create(request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Atualiza um profissional")
    @APIResponse(responseCode = "200", description = "Profissional atualizado com sucesso")
    @APIResponse(responseCode = "400", description = "Profissional e especialidade pertencem a famílias diferentes")
    @APIResponse(responseCode = "404", description = "Profissional ou especialidade não encontrada")
    public ProfessionalResponse update(
            @PathParam("id") UUID id,
            @Valid UpdateProfessionalRequest request
    ) {
        return service.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Remove um profissional")
    @APIResponse(responseCode = "204", description = "Profissional removido com sucesso")
    @APIResponse(responseCode = "404", description = "Profissional não encontrado")
    @APIResponse(responseCode = "409", description = "Profissional possui terapias vinculadas")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}