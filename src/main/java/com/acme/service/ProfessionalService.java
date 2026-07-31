package com.acme.service;

import com.acme.domain.model.Family;
import com.acme.domain.model.Professional;
import com.acme.domain.model.Specialty;
import com.acme.domain.model.Therapy;
import com.acme.dto.request.create.CreateProfessionalRequest;
import com.acme.dto.request.update.UpdateProfessionalRequest;
import com.acme.dto.response.ProfessionalResponse;
import com.acme.mapper.ProfessionalMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProfessionalService {

    @Inject
    ProfessionalMapper mapper;

    @Inject
    FamilyAccessService familyAccessService;

    @Inject
    CurrentUserService currentUserService;

    public List<ProfessionalResponse> list() {
        UUID familyId =
                familyAccessService.getCurrentFamilyId();

        return Professional.<Professional>list(
                        "family.id = ?1 order by name",
                        familyId
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public ProfessionalResponse findById(UUID id) {
        return mapper.toResponse(
                findScopedProfessional(id)
        );
    }

    public List<ProfessionalResponse> listBySpecialty(
            UUID specialtyId
    ) {
        Specialty specialty =
                findScopedSpecialty(specialtyId);

        return Professional.<Professional>list(
                        "specialty.id = ?1 "
                                + "and family.id = ?2 "
                                + "order by name",
                        specialty.getId(),
                        familyAccessService.getCurrentFamilyId()
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public ProfessionalResponse create(
            CreateProfessionalRequest request
    ) {
        currentUserService.requireWritePermission();

        familyAccessService
                .ensureRequestUsesCurrentFamily(
                        request.familyId()
                );

        Family family =
                familyAccessService.getCurrentFamily();

        Specialty specialty =
                findScopedSpecialty(
                        request.specialtyId()
                );

        Professional professional =
                mapper.toEntity(
                        request,
                        family,
                        specialty
                );

        professional.persist();

        return mapper.toResponse(professional);
    }

    @Transactional
    public ProfessionalResponse update(
            UUID id,
            UpdateProfessionalRequest request
    ) {
        currentUserService.requireWritePermission();

        Professional professional =
                findScopedProfessional(id);

        Specialty specialty =
                findScopedSpecialty(
                        request.specialtyId()
                );

        mapper.updateEntity(
                request,
                professional,
                specialty
        );

        return mapper.toResponse(professional);
    }

    @Transactional
    public void delete(UUID id) {
        currentUserService.requireAdmin();

        Professional professional =
                findScopedProfessional(id);

        long therapyCount = Therapy.count(
                "professional.id = ?1",
                professional.getId()
        );

        if (therapyCount > 0) {
            throw new WebApplicationException(
                    "Não é possível excluir um profissional que possui terapias vinculadas.",
                    Response.Status.CONFLICT
            );
        }

        professional.delete();
    }

    private Professional findScopedProfessional(
            UUID id
    ) {
        UUID familyId =
                familyAccessService.getCurrentFamilyId();

        Professional professional = Professional.find(
                "id = ?1 and family.id = ?2",
                id,
                familyId
        ).firstResult();

        if (professional == null) {
            throw new WebApplicationException(
                    "Profissional não encontrado.",
                    Response.Status.NOT_FOUND
            );
        }

        return professional;
    }

    private Specialty findScopedSpecialty(UUID id) {
        UUID familyId =
                familyAccessService.getCurrentFamilyId();

        Specialty specialty = Specialty.find(
                "id = ?1 and family.id = ?2",
                id,
                familyId
        ).firstResult();

        if (specialty == null) {
            throw new WebApplicationException(
                    "Especialidade não encontrada.",
                    Response.Status.NOT_FOUND
            );
        }

        return specialty;
    }
}
