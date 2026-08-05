package com.acme.service;

import com.acme.domain.model.Dependent;
import com.acme.domain.model.Family;
import com.acme.domain.model.User;
import com.acme.dto.request.create.CreateFamilyRequest;
import com.acme.dto.request.update.UpdateFamilyRequest;
import com.acme.dto.response.FamilyResponse;
import com.acme.dto.response.MyFamilyResponse;
import com.acme.mapper.FamilyMapper;
import com.acme.mapper.MyFamilyMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

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

    public List<FamilyResponse> list() {
        currentUserService.requireAdmin();

        return Family.<Family>list("order by name")
                .stream()
                .map(familyMapper::toResponse)
                .toList();
    }

    public FamilyResponse findById(UUID id) {
        currentUserService.requireAdmin();

        Family family =
                familyAccessService.getAccessibleFamily(id);

        return familyMapper.toResponse(family);
    }

    public FamilyResponse getMyFamily() {
        Family family =
                familyAccessService.getCurrentFamily();

        return familyMapper.toResponse(family);
    }

    public MyFamilyResponse getMyFamilyDetails() {
        Family family = familyAccessService.getCurrentFamily();

        UUID familyId = family.getId();

        List<User> users = User.list("family.id = ?1 order by name", familyId);

        List<Dependent> dependents = Dependent.list("family.id = ?1 order by name", familyId);

        return myFamilyMapper.toResponse(
                family,
                users,
                dependents
        );
    }

    @Transactional
    public FamilyResponse create(CreateFamilyRequest request) {
        currentUserService.requireAdmin();

        ensureNameAvailable(request.name(), null);

        Family family = familyMapper.toEntity(request);

        family.persist();

        return familyMapper.toResponse(family);
    }

    @Transactional
    public FamilyResponse update(UUID id, UpdateFamilyRequest request) {
        currentUserService.requireAdmin();

        Family family = familyAccessService.getAccessibleFamily(id);

        ensureNameAvailable(request.name(), family.getId());

        familyMapper.updateEntity(request, family);

        return familyMapper.toResponse(family);
    }

    @Transactional
    public FamilyResponse updateMyFamily(UpdateFamilyRequest request) {
        currentUserService.requireWritePermission();

        Family family = familyAccessService.getCurrentFamily();

        ensureNameAvailable(request.name(), family.getId());

        familyMapper.updateEntity(request, family);

        return familyMapper.toResponse(family);
    }

    private void ensureNameAvailable(String name, UUID ignoredFamilyId) {
        Family existing = Family.find("lower(name) = lower(?1)", name.trim()).firstResult();

        if (existing != null && !existing.getId().equals(ignoredFamilyId)) {

            throw new WebApplicationException("Já existe uma família com este nome.", Response.Status.CONFLICT);
        }
    }
}