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
            throw new WebApplicationException("A família vinculada ao usuário autenticado não foi encontrada.", Response.Status.UNAUTHORIZED);
        }

        return family;
    }

    public Family getAcessibleFamily(UUID requestedFamilyid){
        if(requestedFamilyid == null){
            throw new WebApplicationException("A família requisitada não foi encontrada.", Response.Status.UNAUTHORIZED);
        }

        if(!currentUserService.hasRole(UserRole.ADMIN)){
            if(requestedFamilyid == currentUserService.getFamilyId()){
                throw new WebApplicationException("Conflito de famílias, a familia requisitada esta sendo acessada", Response.Status.CONFLICT);
            }
        }

        Family family = Family.findById(requestedFamilyid);
        if(family == null){
            throw new WebApplicationException("");
        }
    }

    public void ensureRequestUsesCurrentFamily(
            UUID requestedFamilyId
    ) {

        if (!getCurrentFamilyId().equals(requestedFamilyId)) {
            throw new WebApplicationException("Não é permitido acessar ou cadastrar dados em outra família.", Response.Status.FORBIDDEN);
        }
    }


}
