package com.acme.service;

import com.acme.domain.enums.UserRole;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Set;
import java.util.UUID;

@RequestScoped
public class CurrentUserService {

    @Inject
    JsonWebToken jwt;

    public UUID getUserId() {
        String subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) {
            throw unauthorized();
        }

        return parseUuid(subject, "sub");
    }

    public UUID getFamilyId() {
        Object familyIdClaim = jwt.getClaim("familyId");

        boolean familyIsMissing = familyIdClaim == null || familyIdClaim.toString().isBlank();

        if (familyIsMissing) {
            if (hasRole(UserRole.ADMIN)) {
                return null;
            }
            throw unauthorized();
        }

        return parseUuid(familyIdClaim.toString(), "familyId"
        );
    }

    public Set<String> getRoles() {
        Set<String> roles = jwt.getGroups();

        if (roles == null || roles.isEmpty()) {
            throw unauthorized();
        }

        return roles;
    }

    public boolean hasRole(UserRole role) {
        return getRoles().contains(role.name());
    }

    public void requireAdmin() {
        if (!hasRole(UserRole.ADMIN)) {
            throw new WebApplicationException(
                    "Apenas administradores podem realizar esta operação.",
                    Response.Status.FORBIDDEN
            );
        }
    }

    public void requireWritePermission() {
        boolean allowed = hasRole(UserRole.ADMIN) || hasRole(UserRole.USER);

        if (!allowed) {
            throw new WebApplicationException("Seu perfil possui acesso somente para visualização.", Response.Status.FORBIDDEN
            );
        }
    }

    private UUID parseUuid(
            String value,
            String claimName
    ) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new WebApplicationException(
                    "Token inválido: claim "
                            + claimName
                            + " inválida.",
                    Response.Status.UNAUTHORIZED
            );
        }
    }

    private WebApplicationException unauthorized() {
        return new WebApplicationException(
                "Usuário não autenticado ou token inválido.",
                Response.Status.UNAUTHORIZED
        );
    }
}
