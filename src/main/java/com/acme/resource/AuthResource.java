package com.acme.resource;

import com.acme.dto.request.auth.LoginRequest;
import com.acme.dto.response.AuthResponse;
import com.acme.service.AuthService;
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

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(
        name = "Authentication",
        description = "Autenticação de usuários"
)
public class AuthResource {

    @Inject
    AuthService service;

    @POST
    @Path("/login")
    @Operation(summary = "Autentica um usuário")
    @APIResponse(
            responseCode = "200",
            description = "Usuário autenticado com sucesso"
    )
    @APIResponse(
            responseCode = "400",
            description = "Dados inválidos"
    )
    @APIResponse(
            responseCode = "401",
            description = "E-mail ou senha inválidos"
    )
    public AuthResponse login(
            @Valid LoginRequest request
    ) {
        return service.login(request);
    }
}
