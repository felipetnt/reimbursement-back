package com.acme.service;

import com.acme.domain.enums.UserRole;
import com.acme.domain.model.Dependent;
import com.acme.domain.model.Professional;
import com.acme.domain.model.Reimbursement;
import com.acme.domain.model.Therapy;
import com.acme.dto.request.create.CreateTherapyRequest;
import com.acme.dto.request.update.UpdateTherapyRequest;
import com.acme.dto.response.TherapyResponse;
import com.acme.mapper.TherapyMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TherapyService {

    @Inject
    TherapyMapper mapper;

    @Inject
    FamilyAccessService familyAccessService;

    @Inject
    CurrentUserService currentUserService;

    public List<TherapyResponse> list() {
        if (currentUserService.hasRole(UserRole.ADMIN)) {
            return Therapy.<Therapy>list("order by active desc, startDate desc")
                    .stream()
                    .map(mapper::toResponse)
                    .toList();
        }

        UUID familyId = familyAccessService.getCurrentFamilyId();

        return Therapy.<Therapy>list(
                        "dependent.family.id = ?1 order by active desc, startDate desc",
                        familyId
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public TherapyResponse findById(UUID id) {
        return mapper.toResponse(findAccessibleTherapy(id));
    }

    public List<TherapyResponse> listByDependent(UUID dependentId) {
        Dependent dependent = findAccessibleDependent(dependentId);

        return Therapy.<Therapy>list(
                        "dependent.id = ?1 and dependent.family.id = ?2 order by active desc, startDate desc",
                        dependent.getId(),
                        dependent.getFamily().getId()
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public TherapyResponse create(CreateTherapyRequest request) {
        currentUserService.requireWritePermission();

        Dependent dependent = findAccessibleDependent(request.dependentId());
        Professional professional = findAccessibleProfessional(request.professionalId());

        ensureSameFamily(
                dependent.getFamily().getId(),
                professional.getFamily().getId()
        );

        validateDates(request.startDate(), request.endDate());

        ensureNoActiveDuplicate(
                dependent.getId(),
                professional.getId()
        );

        Therapy therapy = mapper.toEntity(
                request,
                dependent,
                professional
        );

        therapy.persist();

        return mapper.toResponse(therapy);
    }

    @Transactional
    public TherapyResponse update(UUID id, UpdateTherapyRequest request) {
        currentUserService.requireWritePermission();

        Therapy therapy = findAccessibleTherapy(id);

        validateDates(request.startDate(), request.endDate());

        mapper.updateEntity(request, therapy);

        return mapper.toResponse(therapy);
    }

    @Transactional
    public void delete(UUID id) {
        currentUserService.requireWritePermission();

        Therapy therapy = findAccessibleTherapy(id);

        long reimbursementCount = Reimbursement.count("therapy.id = ?1", therapy.getId());

        if (reimbursementCount > 0) {
            throw new WebApplicationException(
                    "Não é possível excluir uma terapia que possui reembolsos. Encerre a terapia para preservar o histórico.",
                    Response.Status.CONFLICT
            );
        }

        therapy.delete();
    }

    private Therapy findAccessibleTherapy(UUID id) {
        Therapy therapy;

        if (currentUserService.hasRole(UserRole.ADMIN)) {
            therapy = Therapy.findById(id);
        } else {
            UUID familyId = familyAccessService.getCurrentFamilyId();

            therapy = Therapy.find(
                    "id = ?1 and dependent.family.id = ?2",
                    id,
                    familyId
            ).firstResult();
        }

        if (therapy == null) {
            throw new WebApplicationException("Terapia não encontrada.", Response.Status.NOT_FOUND);
        }

        return therapy;
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

    private Professional findAccessibleProfessional(UUID id) {
        Professional professional;

        if (currentUserService.hasRole(UserRole.ADMIN)) {
            professional = Professional.findById(id);
        } else {
            UUID familyId = familyAccessService.getCurrentFamilyId();
            professional = Professional.find("id = ?1 and family.id = ?2", id, familyId).firstResult();
        }

        if (professional == null) {
            throw new WebApplicationException("Profissional não encontrado.", Response.Status.NOT_FOUND);
        }

        return professional;
    }

    private void ensureSameFamily(UUID dependentFamilyId, UUID professionalFamilyId) {
        if (!dependentFamilyId.equals(professionalFamilyId)) {
            throw new WebApplicationException(
                    "O dependente e o profissional devem pertencer à mesma família.",
                    Response.Status.BAD_REQUEST
            );
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new WebApplicationException(
                    "A data final não pode ser anterior à data inicial.",
                    Response.Status.BAD_REQUEST
            );
        }
    }

    private void ensureNoActiveDuplicate(UUID dependentId, UUID professionalId) {
        long count = Therapy.count(
                "dependent.id = ?1 and professional.id = ?2 and active = true",
                dependentId,
                professionalId
        );

        if (count > 0) {
            throw new WebApplicationException(
                    "Já existe uma terapia ativa entre este dependente e este profissional.",
                    Response.Status.CONFLICT
            );
        }
    }
}