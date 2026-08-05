package com.acme.service;

import com.acme.domain.enums.UserRole;
import com.acme.domain.model.Family;
import com.acme.domain.model.User;
import com.acme.dto.request.auth.ChangePasswordRequest;
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
        if (currentUserService.hasRole(UserRole.ADMIN)) {
            return User.<User>list("order by name")
                    .stream()
                    .map(mapper::toResponse)
                    .toList();
        }

        UUID familyId = familyAccessService.getCurrentFamilyId();

        return User.<User>list("family.id = ?1 order by name", familyId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public UserResponse findById(UUID id) {
        return mapper.toResponse(findAccessibleUser(id));
    }

    public UserResponse getMe() {
        User user = User.findById(currentUserService.getUserId());

        if (user == null) {
            throw new WebApplicationException(
                    "Usuário autenticado não encontrado.",
                    Response.Status.UNAUTHORIZED
            );
        }

        return mapper.toResponse(user);
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        currentUserService.requireWritePermission();

        boolean currentUserIsAdmin = currentUserService.hasRole(UserRole.ADMIN);

        if (!currentUserIsAdmin && request.role() == UserRole.ADMIN) {
            throw new WebApplicationException(
                    "Um usuário não pode criar administradores.",
                    Response.Status.FORBIDDEN
            );
        }

        Family family = resolveFamilyForCreate(request, currentUserIsAdmin);

        ensureEmailAvailable(request.email());

        String passwordHash = passwordService.hash(request.password());

        User user = mapper.toEntity(request, family, passwordHash);
        user.persist();

        return mapper.toResponse(user);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        currentUserService.requireWritePermission();

        User user = findAccessibleUser(id);

        validateRoleUpdate(user, request.role());
        ensureEmailAvailableForUpdate(request.email(), id);

        mapper.updateEntity(request, user);

        if (request.role() == UserRole.ADMIN) {
            user.setFamily(null);
        }

        return mapper.toResponse(user);
    }

    @Transactional
    public void changeMyPassword(ChangePasswordRequest request) {
        User user = User.findById(currentUserService.getUserId());

        if (user == null) {
            throw new WebApplicationException(
                    "Usuário autenticado não encontrado.",
                    Response.Status.UNAUTHORIZED
            );
        }

        if (!passwordService.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new WebApplicationException(
                    "A senha atual está incorreta.",
                    Response.Status.BAD_REQUEST
            );
        }

        if (passwordService.matches(request.newPassword(), user.getPasswordHash())) {
            throw new WebApplicationException("A nova senha deve ser diferente da senha atual.", Response.Status.BAD_REQUEST);
        }

        user.setPasswordHash(passwordService.hash(request.newPassword()));
    }

    @Transactional
    public void delete(UUID id) {
        currentUserService.requireWritePermission();

        User user = findAccessibleUser(id);

        if (user.getId().equals(currentUserService.getUserId())) {
            throw new WebApplicationException("Você não pode excluir o próprio usuário.", Response.Status.CONFLICT);
        }

        ensureCurrentUserCanManage(user);
        ensureIsNotLastAdmin(user);

        user.delete();
    }

    private Family resolveFamilyForCreate(CreateUserRequest request, boolean currentUserIsAdmin) {
        if (request.role() == UserRole.ADMIN) {
            return null;
        }

        if (request.familyId() == null) {
            throw new WebApplicationException(
                    "A família é obrigatória para usuários e visualizadores.",
                    Response.Status.BAD_REQUEST
            );
        }


        if (currentUserIsAdmin) {
            return familyAccessService.getAccessibleFamily(request.familyId());
        }

        familyAccessService.ensureCanAccessFamily(request.familyId());

        return familyAccessService.getCurrentFamily();
    }

    private User findAccessibleUser(UUID id) {
        User user;

        if (currentUserService.hasRole(UserRole.ADMIN)) {
            user = User.findById(id);
        } else {
            UUID familyId = familyAccessService.getCurrentFamilyId();
            user = User.find("id = ?1 and family.id = ?2", id, familyId).firstResult();
        }

        if (user == null) {
            throw new WebApplicationException("Usuário não encontrado.", Response.Status.NOT_FOUND);
        }

        return user;
    }

    private void validateRoleUpdate(User user, UserRole requestedRole) {
        boolean currentUserIsAdmin = currentUserService.hasRole(UserRole.ADMIN);

        if (!currentUserIsAdmin && user.getRole() == UserRole.ADMIN) {
            throw new WebApplicationException("Um usuário da família não pode alterar administradores.", Response.Status.FORBIDDEN);
        }

        if (!currentUserIsAdmin && requestedRole == UserRole.ADMIN) {
            throw new WebApplicationException("Um usuário da família não pode promover usuários a administrador.", Response.Status.FORBIDDEN);
        }

        if (user.getRole() == UserRole.ADMIN && requestedRole != UserRole.ADMIN) {
            throw new WebApplicationException("Não é possível remover o perfil de administrador sem informar uma família.", Response.Status.BAD_REQUEST);
        }
    }

    private void ensureCurrentUserCanManage(User user) {
        if (!currentUserService.hasRole(UserRole.ADMIN)
                && user.getRole() == UserRole.ADMIN) {
            throw new WebApplicationException(
                    "Um usuário da família não pode excluir administradores.",
                    Response.Status.FORBIDDEN
            );
        }
    }

    private void ensureEmailAvailable(String email) {
        String normalizedEmail = normalizeEmail(email);
        long count = User.count("lower(email) = ?1", normalizedEmail);

        if (count > 0) {
            throw new WebApplicationException(
                    "Já existe um usuário com este e-mail.",
                    Response.Status.CONFLICT
            );
        }
    }

    private void ensureEmailAvailableForUpdate(String email, UUID userId) {
        String normalizedEmail = normalizeEmail(email);

        User existing = User.find(
                "lower(email) = ?1",
                normalizedEmail
        ).firstResult();

        if (existing != null && !existing.getId().equals(userId)) {
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

        long adminCount = User.count("role = ?1", UserRole.ADMIN);

        if (adminCount <= 1) {
            throw new WebApplicationException(
                    "O sistema precisa possuir pelo menos um administrador.",
                    Response.Status.CONFLICT
            );
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}