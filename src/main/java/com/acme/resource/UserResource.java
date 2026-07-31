package com.acme.resource;

import com.acme.dto.request.create.CreateUserRequest;
import com.acme.dto.request.update.ChangePasswordRequest;
import com.acme.dto.request.update.UpdateUserRequest;
import com.acme.dto.response.UserResponse;
import com.acme.service.UserService;
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

@Path("/users")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(
        name = "Users",
        description = "Usuários da família autenticada"
)
public class UserResource {

    @Inject
    UserService service;

    @GET
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Lista usuários da família autenticada")
    public List<UserResponse> list() {
        return service.list();
    }

    @GET
    @Path("/me")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Obtém o usuário autenticado")
    public UserResponse getMe() {
        return service.getMe();
    }

    @PUT
    @Path("/me/password")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Altera a senha do usuário autenticado")
    @APIResponse(
            responseCode = "204",
            description = "Senha alterada com sucesso"
    )
    @APIResponse(
            responseCode = "400",
            description = "Senha atual incorreta ou nova senha inválida"
    )
    public Response changeMyPassword(
            @Valid ChangePasswordRequest request
    ) {
        service.changeMyPassword(request);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER", "VIEWER"})
    @Operation(summary = "Busca um usuário da família pelo ID")
    @APIResponse(
            responseCode = "200",
            description = "Usuário encontrado"
    )
    @APIResponse(
            responseCode = "404",
            description = "Usuário não encontrado"
    )
    public UserResponse findById(
            @PathParam("id") UUID id
    ) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed("ADMIN")
    @Operation(summary = "Cria um usuário na família autenticada")
    @APIResponse(
            responseCode = "201",
            description = "Usuário criado com sucesso"
    )
    @APIResponse(
            responseCode = "403",
            description = "Operação permitida somente para administrador"
    )
    @APIResponse(
            responseCode = "409",
            description = "E-mail já cadastrado"
    )
    public Response create(
            @Valid CreateUserRequest request
    ) {
        UserResponse response = service.create(request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Atualiza um usuário da família")
    @APIResponse(
            responseCode = "200",
            description = "Usuário atualizado com sucesso"
    )
    @APIResponse(
            responseCode = "404",
            description = "Usuário não encontrado"
    )
    @APIResponse(
            responseCode = "409",
            description = "E-mail já utilizado"
    )
    public UserResponse update(
            @PathParam("id") UUID id,
            @Valid UpdateUserRequest request
    ) {
        return service.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Remove um usuário da família")
    @APIResponse(
            responseCode = "204",
            description = "Usuário removido com sucesso"
    )
    @APIResponse(
            responseCode = "404",
            description = "Usuário não encontrado"
    )
    @APIResponse(
            responseCode = "409",
            description = "Usuário não pode ser removido"
    )
    public Response delete(
            @PathParam("id") UUID id
    ) {
        service.delete(id);
        return Response.noContent().build();
    }
}
