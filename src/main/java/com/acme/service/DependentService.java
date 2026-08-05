package com.acme.service;

import com.acme.domain.enums.UserRole;
import com.acme.domain.model.Dependent;
import com.acme.domain.model.Family;
import com.acme.domain.model.Reimbursement;
import com.acme.domain.model.Therapy;
import com.acme.dto.request.create.CreateDependentRequest;
import com.acme.dto.request.update.UpdateDependentRequest;
import com.acme.dto.response.DependentDetailsResponse;
import com.acme.dto.response.DependentResponse;
import com.acme.mapper.DependentDetailsMapper;
import com.acme.mapper.DependentMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DependentService {

    @Inject
    DependentMapper mapper;

    @Inject
    DependentDetailsMapper detailsMapper;

    @Inject
    FamilyAccessService familyAccessService;

    @Inject
    CurrentUserService currentUserService;

    public List<DependentResponse> list() {
        if (currentUserService.hasRole(UserRole.ADMIN)) {
            return Dependent.<Dependent>list("order by name")
                    .stream()
                    .map(mapper::toResponse)
                    .toList();
        }

        UUID familyId = familyAccessService.getCurrentFamilyId();

        return Dependent.<Dependent>list("family.id = ?1 order by name", familyId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public DependentResponse findById(UUID id) {
        return mapper.toResponse(findAccessibleDependent(id));
    }

    public DependentDetailsResponse findDetails(UUID id) {
        Dependent dependent = findAccessibleDependent(id);
        UUID familyId = dependent.getFamily().getId();

        List<Therapy> therapies = Therapy.list(
                "dependent.id = ?1 and dependent.family.id = ?2 order by active desc, startDate desc",
                dependent.getId(),
                familyId
        );

        List<Reimbursement> reimbursements = Reimbursement.list(
                "therapy.dependent.id = ?1 and therapy.dependent.family.id = ?2 order by referenceMonth desc",
                dependent.getId(),
                familyId
        );

        return detailsMapper.toResponse(
                dependent,
                therapies,
                reimbursements
        );
    }

    @Transactional
    public DependentResponse create(CreateDependentRequest request) {
        currentUserService.requireWritePermission();

        Family family = familyAccessService.getAccessibleFamily(request.familyId());

        Dependent dependent = mapper.toEntity(request, family);
        dependent.persist();

        return mapper.toResponse(dependent);
    }

    @Transactional
    public DependentResponse update(UUID id, UpdateDependentRequest request) {
        currentUserService.requireWritePermission();

        Dependent dependent = findAccessibleDependent(id);

        mapper.updateEntity(request, dependent);

        return mapper.toResponse(dependent);
    }

    @Transactional
    public void delete(UUID id) {
        currentUserService.requireWritePermission();

        Dependent dependent = findAccessibleDependent(id);

        long therapyCount = Therapy.count("dependent.id = ?1", dependent.getId());

        if (therapyCount > 0) {
            throw new WebApplicationException(
                    "Não é possível excluir um dependente que possui terapias ou histórico financeiro.",
                    Response.Status.CONFLICT
            );
        }

        dependent.delete();
    }

    private Dependent findAccessibleDependent(UUID id) {
        Dependent dependent;

        if (currentUserService.hasRole(UserRole.ADMIN)) {
            dependent = Dependent.findById(id);
        } else {
            UUID familyId = familyAccessService.getCurrentFamilyId();
            dependent = Dependent.find("id = ?1 and family.id = ?2", id, familyId).firstResult();
        }

        if (dependent == null) {
            throw new WebApplicationException("Dependente não encontrado.", Response.Status.NOT_FOUND);
        }

        return dependent;
    }
}