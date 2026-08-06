package com.acme.service;

import com.acme.domain.enums.UserRole;
import com.acme.domain.model.Family;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@ApplicationScoped
public class FamilyAccessService {

    @Inject
    CurrentUserService currentUserService;

    public UUID getCurrentFamilyId() {
        return currentUserService.getFamilyId();
    }

    public Family getCurrentFamily() {
        UUID familyId = getCurrentFamilyId();

        Family family = Family.findById(familyId);

        if (family == null) {
            throw new WebApplicationException(
                    "A família vinculada ao usuário autenticado não foi encontrada.",
                    Response.Status.UNAUTHORIZED
            );
        }

        return family;
    }

    public Family getAccessibleFamily(UUID requestedFamilyId) {
        ensureCanAccessFamily(requestedFamilyId);

        Family family = Family.findById(requestedFamilyId);

        if (family == null) {
            throw new WebApplicationException("Família não encontrada.", Response.Status.NOT_FOUND);
        }

        return family;
    }

    public void ensureCanAccessFamily(UUID requestedFamilyId) {
        if (requestedFamilyId == null) {
            throw new WebApplicationException("O ID da família é obrigatório.", Response.Status.BAD_REQUEST);
        }

        if (currentUserService.hasRole(UserRole.ADMIN)) {
            return;
        }

        UUID currentFamilyId = getCurrentFamilyId();

        if (!currentFamilyId.equals(requestedFamilyId)) {
            throw new WebApplicationException("Você não possui permissão para acessar outra família.", Response.Status.FORBIDDEN);
        }
    }
}