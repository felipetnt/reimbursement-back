package com.acme.service;

import com.acme.domain.model.Family;
import com.acme.domain.model.User;
import com.acme.dto.request.create.CreateUserRequest;
import com.acme.dto.request.update.UpdateUserRequest;
import com.acme.dto.response.UserResponse;
import com.acme.mapper.UserMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    @Inject
    UserMapper mapper;

    public List<UserResponse> list() {
        return User.<User>listAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse findById(UUID id) {
        User user = User.findById(id);

        ensureUserExists(user);

        return mapper.toResponse(user);
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        ensureEmailAvailable(request.email());

        Family family = Family.findById(request.familyId());

        ensureFamilyExists(family);

        User user = mapper.toEntity(request, family);

        user.persist();

        return mapper.toResponse(user);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User userBeforeUpdate = User.findById(id);

        ensureUserExists(userBeforeUpdate);

        Family family = Family.findById(request.familyId());

        ensureFamilyExists(family);

        ensureEmailAvailableForUpdate(request.email(), id);

        User user = mapper.updateEntity(request, userBeforeUpdate, family);

        return mapper.toResponse(user);
    }

    @Transactional
    public void delete(UUID id) {
        User user = User.findById(id);

        ensureUserExists(user);

        user.delete();
    }

    private void ensureFamilyExists(Family family) {
        if (family == null) {
            throw new WebApplicationException(
                    "Família não encontrada.",
                    Response.Status.NOT_FOUND
            );
        }
    }

    private void ensureUserExists(User user) {
        if (user == null) {
            throw new WebApplicationException(
                    "Usuário não encontrado.",
                    Response.Status.NOT_FOUND
            );
        }
    }

    private void ensureEmailAvailable(String email) {
        if (User.count("email", email) > 0) {
            throw new WebApplicationException(
                    "Já existe um usuário com este e-mail.",
                    Response.Status.CONFLICT
            );
        }
    }

    private void ensureEmailAvailableForUpdate(String email, UUID id) {
        User existing = User.find("email", email).firstResult();

        if (existing != null && !existing.getId().equals(id)) {
            throw new WebApplicationException(
                    "Já existe um usuário com este e-mail.",
                    Response.Status.CONFLICT
            );
        }
    }
}