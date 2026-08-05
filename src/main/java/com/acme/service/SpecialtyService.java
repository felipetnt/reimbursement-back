package com.acme.service;

import com.acme.domain.enums.UserRole;
import com.acme.domain.model.Family;
import com.acme.domain.model.Professional;
import com.acme.domain.model.Specialty;
import com.acme.dto.request.create.CreateSpecialtyRequest;
import com.acme.dto.request.update.UpdateSpecialtyRequest;
import com.acme.dto.response.SpecialtyResponse;
import com.acme.mapper.SpecialtyMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SpecialtyService {

    @Inject
    SpecialtyMapper mapper;

    @Inject
    FamilyAccessService familyAccessService;

    @Inject
    CurrentUserService currentUserService;

    public List<SpecialtyResponse> list() {
        if (currentUserService.hasRole(UserRole.ADMIN)) {
            return Specialty.<Specialty>list("order by name")
                    .stream()
                    .map(mapper::toResponse)
                    .toList();
        }

        UUID familyId = familyAccessService.getCurrentFamilyId();

        return Specialty.<Specialty>list("family.id = ?1 order by name", familyId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public SpecialtyResponse findById(UUID id) {
        return mapper.toResponse(findAccessibleSpecialty(id));
    }

    @Transactional
    public SpecialtyResponse create(CreateSpecialtyRequest request) {
        currentUserService.requireWritePermission();

        Family family = familyAccessService.getAccessibleFamily(request.familyId());

        ensureNameAvailable(request.name(), family.getId(), null);

        Specialty specialty = mapper.toEntity(request, family);
        specialty.persist();

        return mapper.toResponse(specialty);
    }

    @Transactional
    public SpecialtyResponse update(UUID id, UpdateSpecialtyRequest request) {
        currentUserService.requireWritePermission();

        Specialty specialty = findAccessibleSpecialty(id);
        UUID familyId = specialty.getFamily().getId();

        ensureNameAvailable(request.name(), familyId, specialty.getId());

        mapper.updateEntity(request, specialty);

        return mapper.toResponse(specialty);
    }

    @Transactional
    public void delete(UUID id) {
        currentUserService.requireWritePermission();

        Specialty specialty = findAccessibleSpecialty(id);

        long professionalCount = Professional.count("specialty.id = ?1", specialty.getId());

        if (professionalCount > 0) {
            throw new WebApplicationException(
                    "Não é possível excluir uma especialidade que possui profissionais vinculados.",
                    Response.Status.CONFLICT
            );
        }

        specialty.delete();
    }

    private Specialty findAccessibleSpecialty(UUID id) {
        Specialty specialty;

        if (currentUserService.hasRole(UserRole.ADMIN)) {
            specialty = Specialty.findById(id);
        } else {
            UUID familyId = familyAccessService.getCurrentFamilyId();
            specialty = Specialty.find("id = ?1 and family.id = ?2", id, familyId).firstResult();
        }

        if (specialty == null) {
            throw new WebApplicationException("Especialidade não encontrada.", Response.Status.NOT_FOUND);
        }

        return specialty;
    }

    private void ensureNameAvailable(String name, UUID familyId, UUID ignoredId) {
        Specialty existing = Specialty.find(
                "lower(name) = lower(?1) and family.id = ?2",
                name.trim(),
                familyId
        ).firstResult();

        if (existing != null && !existing.getId().equals(ignoredId)) {
            throw new WebApplicationException(
                    "Já existe uma especialidade com este nome nesta família.",
                    Response.Status.CONFLICT
            );
        }
    }
}