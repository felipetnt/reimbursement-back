package com.acme.service;

import com.acme.domain.model.Dependent;
import com.acme.domain.model.Family;
import com.acme.domain.model.User;
import com.acme.dto.request.update.UpdateFamilyRequest;
import com.acme.dto.response.FamilyResponse;
import com.acme.dto.response.MyFamilyResponse;
import com.acme.mapper.FamilyMapper;
import com.acme.mapper.MyFamilyMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FamilyService {

    @Inject
    FamilyAccessService familyAccessService;

    @Inject
    CurrentUserService currentUserService;

    @Inject
    FamilyMapper familyMapper;

    @Inject
    MyFamilyMapper myFamilyMapper;

    public FamilyResponse getMyFamily() {
        Family family =
                familyAccessService.getCurrentFamily();

        return familyMapper.toResponse(family);
    }

    public MyFamilyResponse getMyFamilyDetails() {
        Family family =
                familyAccessService.getCurrentFamily();

        UUID familyId = family.getId();

        List<User> users = User.list(
                "family.id = ?1 order by name",
                familyId
        );

        List<Dependent> dependents = Dependent.list(
                "family.id = ?1 order by name",
                familyId
        );

        return myFamilyMapper.toResponse(
                family,
                users,
                dependents
        );
    }

    @Transactional
    public FamilyResponse updateMyFamily(
            UpdateFamilyRequest request
    ) {
        currentUserService.requireAdmin();

        Family family =
                familyAccessService.getCurrentFamily();

        familyMapper.updateEntity(request, family);

        return familyMapper.toResponse(family);
    }
}
