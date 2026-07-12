package com.acme.resource;

import com.acme.dto.request.create.CreateUserRequest;
import com.acme.dto.request.update.UpdateUserRequest;
import com.acme.dto.response.UserResponse;
import com.acme.service.UserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;
import java.util.UUID;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService service;

    @GET
    @Operation(summary = "Lista todos os usuários")
    public List<UserResponse> list() {

        return service.list();

    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca usuário pelo ID")
    @APIResponse(responseCode = "200", description = "Usuário encontrado")
    @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    public UserResponse findById(@PathParam("id") UUID id) {

        return service.findById(id);

    }

    @POST
    @Operation(summary = "Cria um novo usuário")
    @APIResponse(responseCode = "201", description = "Usuário criado")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "409", description = "Email já cadastrado")
    public Response create(@Valid CreateUserRequest request) {

        UserResponse response = service.create(request);

        return Response.status(Response.Status.CREATED).entity(response).build();

    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualiza um usuário")
    @APIResponse(responseCode = "200", description = "Usuário atualizado")
    @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    @APIResponse(responseCode = "409", description = "Email já utilizado")
    public UserResponse update(@PathParam("id") UUID id, @Valid UpdateUserRequest request) {

        return service.update(id, request);

    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remove um usuário")
    @APIResponse(responseCode = "204", description = "Usuário removido")
    @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    public Response delete(@PathParam("id") UUID id) {

        service.delete(id);

        return Response.noContent().build();

    }

}