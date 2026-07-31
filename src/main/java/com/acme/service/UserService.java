package com.acme.service;

import com.acme.domain.enums.UserRole;
import com.acme.domain.model.Family;
import com.acme.domain.model.User;
import com.acme.dto.request.create.CreateUserRequest;
import com.acme.dto.request.update.ChangePasswordRequest;
import com.acme.dto.request.update.UpdateUserRequest;
import com.acme.dto.response.UserResponse;
import com.acme.mapper.UserMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    UserMapper mapper;

    @Inject
    PasswordService passwordService;

    @Inject
    FamilyAccessService familyAccessService;

    @Inject
    CurrentUserService currentUserService;

    public List<UserResponse> list() {
        UUID familyId =
                familyAccessService.getCurrentFamilyId();

        return User.<User>list(
                        "family.id = ?1 order by name",
                        familyId
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public UserResponse findById(UUID id) {
        return mapper.toResponse(findScopedUser(id));
    }

    public UserResponse getMe() {
        return mapper.toResponse(
                findScopedUser(
                        currentUserService.getUserId()
                )
        );
    }

    @Transactional
    public UserResponse create(
            CreateUserRequest request
    ) {
        currentUserService.requireAdmin();

        familyAccessService
                .ensureRequestUsesCurrentFamily(
                        request.familyId()
                );

        ensureEmailAvailable(request.email());

        Family family =
                familyAccessService.getCurrentFamily();

        String passwordHash =
                passwordService.hash(request.password());

        User user = mapper.toEntity(
                request,
                family,
                passwordHash
        );

        user.persist();

        return mapper.toResponse(user);
    }

    @Transactional
    public UserResponse update(
            UUID id,
            UpdateUserRequest request
    ) {
        currentUserService.requireAdmin();

        User user = findScopedUser(id);

        ensureEmailAvailableForUpdate(
                request.email(),
                id
        );

        mapper.updateEntity(request, user);

        return mapper.toResponse(user);
    }

    @Transactional
    public void changeMyPassword(
            ChangePasswordRequest request
    ) {
        User user = findScopedUser(
                currentUserService.getUserId()
        );

        if (!passwordService.matches(
                request.currentPassword(),
                user.getPasswordHash()
        )) {
            throw new WebApplicationException(
                    "A senha atual está incorreta.",
                    Response.Status.BAD_REQUEST
            );
        }

        if (passwordService.matches(
                request.newPassword(),
                user.getPasswordHash()
        )) {
            throw new WebApplicationException(
                    "A nova senha deve ser diferente da senha atual.",
                    Response.Status.BAD_REQUEST
            );
        }

        user.setPasswordHash(
                passwordService.hash(
                        request.newPassword()
                )
        );
    }

    @Transactional
    public void delete(UUID id) {
        currentUserService.requireAdmin();

        User user = findScopedUser(id);

        if (user.getId().equals(
                currentUserService.getUserId()
        )) {
            throw new WebApplicationException(
                    "Você não pode excluir o próprio usuário.",
                    Response.Status.CONFLICT
            );
        }

        ensureIsNotLastAdmin(user);

        user.delete();
    }

    private User findScopedUser(UUID id) {
        UUID familyId =
                familyAccessService.getCurrentFamilyId();

        User user = User.find(
                "id = ?1 and family.id = ?2",
                id,
                familyId
        ).firstResult();

        if (user == null) {
            throw new WebApplicationException(
                    "Usuário não encontrado.",
                    Response.Status.NOT_FOUND
            );
        }

        return user;
    }

    private void ensureEmailAvailable(String email) {
        String normalizedEmail = normalizeEmail(email);

        long count = User.count(
                "lower(email) = ?1",
                normalizedEmail
        );

        if (count > 0) {
            throw new WebApplicationException(
                    "Já existe um usuário com este e-mail.",
                    Response.Status.CONFLICT
            );
        }
    }

    private void ensureEmailAvailableForUpdate(
            String email,
            UUID userId
    ) {
        String normalizedEmail = normalizeEmail(email);

        User existing = User.find(
                "lower(email) = ?1",
                normalizedEmail
        ).firstResult();

        if (existing != null
                && !existing.getId().equals(userId)) {
            throw new WebApplicationException(
                    "Já existe um usuário com este e-mail.",
                    Response.Status.CONFLICT
            );
        }
    }

    private void ensureIsNotLastAdmin(User user) {
        if (user.getRole() != UserRole.ADMIN) {
            return;
        }

        long adminCount = User.count(
                "family.id = ?1 and role = ?2",
                familyAccessService.getCurrentFamilyId(),
                UserRole.ADMIN
        );

        if (adminCount <= 1) {
            throw new WebApplicationException(
                    "A família precisa possuir pelo menos um administrador.",
                    Response.Status.CONFLICT
            );
        }
    }

    private String normalizeEmail(String email) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
