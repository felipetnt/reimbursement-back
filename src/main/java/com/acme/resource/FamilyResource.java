package com.acme.resource;

import com.acme.dto.request.update.UpdateFamilyRequest;
import com.acme.dto.response.FamilyResponse;
import com.acme.dto.response.MyFamilyResponse;
import com.acme.service.FamilyService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/families")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(
        name = "Families",
        description = "Dados da família autenticada"
)
public class FamilyResource {

    @Inject
    FamilyService service;

    @GET
    @Path("/me")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Obtém a família autenticada")
    @APIResponse(
            responseCode = "200",
            description = "Família encontrada"
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado"
    )
    public FamilyResponse getMyFamily() {
        return service.getMyFamily();
    }

    @GET
    @Path("/me/details")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(
            summary = "Obtém responsáveis e dependentes da família autenticada"
    )
    @APIResponse(
            responseCode = "200",
            description = "Detalhes da família encontrados"
    )
    public MyFamilyResponse getMyFamilyDetails() {
        return service.getMyFamilyDetails();
    }

    @PUT
    @Path("/me")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Atualiza a família autenticada")
    @APIResponse(
            responseCode = "200",
            description = "Família atualizada com sucesso"
    )
    @APIResponse(
            responseCode = "400",
            description = "Dados inválidos"
    )
    @APIResponse(
            responseCode = "403",
            description = "Operação permitida somente para administrador"
    )
    public FamilyResponse updateMyFamily(
            @Valid UpdateFamilyRequest request
    ) {
        return service.updateMyFamily(request);
    }
}
