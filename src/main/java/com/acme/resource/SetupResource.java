package com.acme.resource;

import com.acme.dto.request.auth.InitializeSystemRequest;
import com.acme.dto.request.auth.AuthResponse;
import com.acme.service.SetupService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/setup")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(
        name = "Setup",
        description = "Inicialização única do sistema"
)
public class SetupResource {

    @Inject
    SetupService service;

    @POST
    @Path("/initialize")
    @Operation(
            summary = "Cria a primeira família e o primeiro administrador"
    )
    @APIResponse(
            responseCode = "200",
            description = "Sistema inicializado e usuário autenticado"
    )
    @APIResponse(
            responseCode = "400",
            description = "Dados inválidos"
    )
    @APIResponse(
            responseCode = "409",
            description = "Sistema já inicializado"
    )
    public AuthResponse initialize(
            @Valid InitializeSystemRequest request
    ) {
        return service.initialize(request);
    }
}
