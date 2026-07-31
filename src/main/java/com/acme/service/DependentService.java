package com.acme.service;

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
        UUID familyId =
                familyAccessService.getCurrentFamilyId();

        return Dependent.<Dependent>list(
                        "family.id = ?1 order by name",
                        familyId
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public DependentResponse findById(UUID id) {
        return mapper.toResponse(
                findScopedDependent(id)
        );
    }

    public DependentDetailsResponse findDetails(
            UUID id
    ) {
        Dependent dependent =
                findScopedDependent(id);

        UUID familyId =
                familyAccessService.getCurrentFamilyId();

        List<Therapy> therapies = Therapy.list(
                "dependent.id = ?1 "
                        + "and dependent.family.id = ?2 "
                        + "order by active desc, "
                        + "startDate desc",
                dependent.getId(),
                familyId
        );

        List<Reimbursement> reimbursements =
                Reimbursement.list(
                        "therapy.dependent.id = ?1 "
                                + "and therapy.dependent."
                                + "family.id = ?2 "
                                + "order by "
                                + "referenceMonth desc",
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
    public DependentResponse create(
            CreateDependentRequest request
    ) {
        currentUserService.requireWritePermission();

        familyAccessService
                .ensureRequestUsesCurrentFamily(
                        request.familyId()
                );

        Family family =
                familyAccessService.getCurrentFamily();

        Dependent dependent = mapper.toEntity(
                request,
                family
        );

        dependent.persist();

        return mapper.toResponse(dependent);
    }

    @Transactional
    public DependentResponse update(
            UUID id,
            UpdateDependentRequest request
    ) {
        currentUserService.requireWritePermission();

        Dependent dependent =
                findScopedDependent(id);

        mapper.updateEntity(request, dependent);

        return mapper.toResponse(dependent);
    }

    @Transactional
    public void delete(UUID id) {
        currentUserService.requireAdmin();

        Dependent dependent =
                findScopedDependent(id);

        long therapyCount = Therapy.count(
                "dependent.id = ?1",
                dependent.getId()
        );

        if (therapyCount > 0) {
            throw new WebApplicationException(
                    "Não é possível excluir um dependente que possui terapias ou histórico financeiro.",
                    Response.Status.CONFLICT
            );
        }

        dependent.delete();
    }

    private Dependent findScopedDependent(UUID id) {
        UUID familyId =
                familyAccessService.getCurrentFamilyId();

        Dependent dependent = Dependent.find(
                "id = ?1 and family.id = ?2",
                id,
                familyId
        ).firstResult();

        if (dependent == null) {
            throw new WebApplicationException(
                    "Dependente não encontrado.",
                    Response.Status.NOT_FOUND
            );
        }

        return dependent;
    }
}
