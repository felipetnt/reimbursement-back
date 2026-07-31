package com.acme.service;

import com.acme.domain.enums.SolicitationStatus;
import com.acme.domain.model.Dependent;
import com.acme.domain.model.Reimbursement;
import com.acme.domain.model.Solicitation;
import com.acme.domain.model.Therapy;
import com.acme.dto.request.create.CreateReimbursementRequest;
import com.acme.dto.request.update.UpdateReimbursementRequest;
import com.acme.dto.response.ReimbursementDetailsResponse;
import com.acme.dto.response.ReimbursementResponse;
import com.acme.mapper.ReimbursementDetailsMapper;
import com.acme.mapper.ReimbursementMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ReimbursementService {

    @Inject
    ReimbursementMapper mapper;

    @Inject
    ReimbursementDetailsMapper detailsMapper;

    @Inject
    FamilyAccessService familyAccessService;

    @Inject
    CurrentUserService currentUserService;

    public List<ReimbursementResponse> list() {
        UUID familyId =
                familyAccessService.getCurrentFamilyId();

        return Reimbursement.<Reimbursement>list(
                        "therapy.dependent.family.id = ?1 "
                                + "order by referenceMonth desc",
                        familyId
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public ReimbursementResponse findById(UUID id) {
        return mapper.toResponse(
                findScopedReimbursement(id)
        );
    }

    public ReimbursementDetailsResponse findDetails(
            UUID id
    ) {
        Reimbursement reimbursement =
                findScopedReimbursement(id);

        List<Solicitation> solicitations =
                Solicitation.list(
                        "reimbursement.id = ?1 "
                                + "order by "
                                + "attemptNumber asc",
                        reimbursement.getId()
                );

        return detailsMapper.toResponse(
                reimbursement,
                solicitations
        );
    }

    public List<ReimbursementResponse> listByDependent(
            UUID dependentId
    ) {
        Dependent dependent =
                findScopedDependent(dependentId);

        return Reimbursement.<Reimbursement>list(
                        "therapy.dependent.id = ?1 "
                                + "and therapy.dependent."
                                + "family.id = ?2 "
                                + "order by "
                                + "referenceMonth desc",
                        dependent.getId(),
                        familyAccessService.getCurrentFamilyId()
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public ReimbursementResponse create(
            CreateReimbursementRequest request
    ) {
        currentUserService.requireWritePermission();

        Therapy therapy =
                findScopedTherapy(request.therapyId());

        LocalDate referenceMonth =
                normalizeMonth(
                        request.referenceMonth()
                );

        validateReferenceMonth(
                therapy,
                referenceMonth
        );

        ensureMonthAvailable(
                therapy.getId(),
                referenceMonth,
                null
        );

        Reimbursement reimbursement =
                mapper.toEntity(request, therapy);

        reimbursement.setReferenceMonth(
                referenceMonth
        );

        reimbursement.persist();

        Solicitation initialSolicitation =
                new Solicitation();

        initialSolicitation.setReimbursement(
                reimbursement
        );
        initialSolicitation.setAttemptNumber(1);
        initialSolicitation.setStatus(
                SolicitationStatus.NOT_REQUESTED
        );
        initialSolicitation.persist();

        return mapper.toResponse(reimbursement);
    }

    @Transactional
    public ReimbursementResponse update(
            UUID id,
            UpdateReimbursementRequest request
    ) {
        currentUserService.requireWritePermission();

        Reimbursement reimbursement =
                findScopedReimbursement(id);

        ensureReimbursementIsDraft(reimbursement);

        LocalDate referenceMonth =
                normalizeMonth(
                        request.referenceMonth()
                );

        validateReferenceMonth(
                reimbursement.getTherapy(),
                referenceMonth
        );

        ensureMonthAvailable(
                reimbursement.getTherapy().getId(),
                referenceMonth,
                reimbursement.getId()
        );

        mapper.updateEntity(
                request,
                reimbursement
        );

        reimbursement.setReferenceMonth(
                referenceMonth
        );

        return mapper.toResponse(reimbursement);
    }

    @Transactional
    public void delete(UUID id) {
        currentUserService.requireAdmin();

        Reimbursement reimbursement =
                findScopedReimbursement(id);

        ensureReimbursementIsDraft(reimbursement);

        Solicitation.delete(
                "reimbursement.id = ?1",
                reimbursement.getId()
        );

        reimbursement.delete();
    }

    private Reimbursement findScopedReimbursement(
            UUID id
    ) {
        UUID familyId =
                familyAccessService.getCurrentFamilyId();

        Reimbursement reimbursement =
                Reimbursement.find(
                        "id = ?1 "
                                + "and therapy.dependent."
                                + "family.id = ?2",
                        id,
                        familyId
                ).firstResult();

        if (reimbursement == null) {
            throw new WebApplicationException(
                    "Reembolso não encontrado.",
                    Response.Status.NOT_FOUND
            );
        }

        return reimbursement;
    }

    private Therapy findScopedTherapy(UUID id) {
        UUID familyId =
                familyAccessService.getCurrentFamilyId();

        Therapy therapy = Therapy.find(
                "id = ?1 "
                        + "and dependent.family.id = ?2",
                id,
                familyId
        ).firstResult();

        if (therapy == null) {
            throw new WebApplicationException(
                    "Terapia não encontrada.",
                    Response.Status.NOT_FOUND
            );
        }

        return therapy;
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

    private void ensureMonthAvailable(
            UUID therapyId,
            LocalDate referenceMonth,
            UUID ignoredId
    ) {
        Reimbursement existing =
                Reimbursement.find(
                        "therapy.id = ?1 "
                                + "and referenceMonth = ?2",
                        therapyId,
                        referenceMonth
                ).firstResult();

        if (existing != null
                && !existing.getId().equals(ignoredId)) {
            throw new WebApplicationException(
                    "Já existe um reembolso para esta terapia no mês informado.",
                    Response.Status.CONFLICT
            );
        }
    }

    private void ensureReimbursementIsDraft(
            Reimbursement reimbursement
    ) {
        long submittedCount = Solicitation.count(
                "reimbursement.id = ?1 "
                        + "and status <> ?2",
                reimbursement.getId(),
                SolicitationStatus.NOT_REQUESTED
        );

        if (submittedCount > 0) {
            throw new WebApplicationException(
                    "Este reembolso já possui histórico de solicitação e não pode ser alterado ou excluído.",
                    Response.Status.CONFLICT
            );
        }
    }

    private void validateReferenceMonth(
            Therapy therapy,
            LocalDate referenceMonth
    ) {
        YearMonth requested =
                YearMonth.from(referenceMonth);

        YearMonth therapyStart =
                YearMonth.from(therapy.getStartDate());

        if (requested.isBefore(therapyStart)) {
            throw new WebApplicationException(
                    "O mês de referência não pode ser anterior ao início da terapia.",
                    Response.Status.BAD_REQUEST
            );
        }

        if (therapy.getEndDate() == null) {
            return;
        }

        YearMonth therapyEnd =
                YearMonth.from(therapy.getEndDate());

        if (requested.isAfter(therapyEnd)) {
            throw new WebApplicationException(
                    "O mês de referência não pode ser posterior ao encerramento da terapia.",
                    Response.Status.BAD_REQUEST
            );
        }
    }

    private LocalDate normalizeMonth(
            LocalDate referenceMonth
    ) {
        return referenceMonth.withDayOfMonth(1);
    }
}
