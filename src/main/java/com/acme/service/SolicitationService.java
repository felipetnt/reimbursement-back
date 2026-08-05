package com.acme.service;

import com.acme.domain.enums.SolicitationStatus;
import com.acme.domain.model.Reimbursement;
import com.acme.domain.model.Solicitation;
import com.acme.dto.request.create.CreateSolicitationRequest;
import com.acme.dto.request.update.UpdateSolicitationRequest;
import com.acme.dto.response.SolicitationResponse;
import com.acme.mapper.SolicitationMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SolicitationService {

    @Inject
    SolicitationMapper mapper;

    @Inject
    FamilyAccessService familyAccessService;

    @Inject
    CurrentUserService currentUserService;

    public List<SolicitationResponse> list() {
        UUID familyId =
                familyAccessService.getCurrentFamilyId();

        return Solicitation.<Solicitation>list(
                        "reimbursement.therapy.dependent."
                                + "family.id = ?1 "
                                + "order by reimbursement."
                                + "referenceMonth desc, "
                                + "attemptNumber asc",
                        familyId
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public SolicitationResponse findById(UUID id) {
        return mapper.toResponse(
                findScopedSolicitation(id)
        );
    }

    public List<SolicitationResponse>
    listByReimbursement(UUID reimbursementId) {

        Reimbursement reimbursement =
                findScopedReimbursement(
                        reimbursementId
                );

        return Solicitation.<Solicitation>list(
                        "reimbursement.id = ?1 "
                                + "order by "
                                + "attemptNumber asc",
                        reimbursement.getId()
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public SolicitationResponse create(
            CreateSolicitationRequest request
    ) {
        currentUserService.requireWritePermission();

        Reimbursement reimbursement =
                findScopedReimbursement(
                        request.reimbursementId()
                );

        ensureCanCreateNewAttempt(reimbursement);

        Integer attemptNumber =
                getNextAttemptNumber(
                        reimbursement.getId()
                );

        Solicitation solicitation =
                mapper.toEntity(
                        request,
                        reimbursement,
                        attemptNumber,
                        SolicitationStatus.NOT_REQUESTED
                );

        solicitation.persist();

        return mapper.toResponse(solicitation);
    }

    @Transactional
    public SolicitationResponse update(UUID id, UpdateSolicitationRequest request) {
        currentUserService.requireWritePermission();

        Solicitation solicitation = findScopedSolicitation(id);

        validateStatusTransition(solicitation.getStatus(), request.status()
        );

        validateStatusRules(
                request.status(),
                request.protocolNumber(),
                request.requestDate(),
                request.reimbursementDate(),
                request.amountReceived()
        );

        ensureProtocolAvailable(
                request.protocolNumber(),
                solicitation.getReimbursement().getId(),
                solicitation.getId()
        );

        mapper.updateEntity(request, solicitation);

        return mapper.toResponse(solicitation);
    }

    private Solicitation findScopedSolicitation(
            UUID id
    ) {
        UUID familyId =
                familyAccessService.getCurrentFamilyId();

        Solicitation solicitation =
                Solicitation.find("id = ?1 and reimbursement.therapy.dependent.family.id = ?2", id, familyId).firstResult();

        if (solicitation == null) {
            throw new WebApplicationException("Solicitação não encontrada.", Response.Status.NOT_FOUND);
        }

        return solicitation;
    }

    private Reimbursement findScopedReimbursement(UUID id) {
        UUID familyId = familyAccessService.getCurrentFamilyId();

        Reimbursement reimbursement = Reimbursement.find("id = ?1 and therapy.dependent.family.id = ?2", id, familyId).firstResult();

        if (reimbursement == null) {
            throw new WebApplicationException(
                    "Reembolso não encontrado.",
                    Response.Status.NOT_FOUND
            );
        }

        return reimbursement;
    }

    private Integer getNextAttemptNumber(UUID reimbursementId) {
        Solicitation last = Solicitation.find("reimbursement.id = ?1 order by attemptNumber desc", reimbursementId).firstResult();

        return last == null ? 1 : last.getAttemptNumber() + 1;
    }

    private void ensureCanCreateNewAttempt(Reimbursement reimbursement) {
        Solicitation last = Solicitation.find("reimbursement.id = ?1 " + "order by attemptNumber desc", reimbursement.getId()).firstResult();

        if (last != null && last.getStatus() != SolicitationStatus.DENIED) {
            throw new WebApplicationException("Uma nova tentativa só pode ser criada quando a última solicitação estiver negada.", Response.Status.CONFLICT);
        }
    }

    private void validateStatusRules(
            SolicitationStatus status,
            String protocolNumber,
            LocalDate requestDate,
            LocalDate reimbursementDate,
            BigDecimal amountReceived
    ) {
        if (status == null) {
            throw new WebApplicationException("Status é obrigatório.",Response.Status.BAD_REQUEST);
        }

        if (status == SolicitationStatus.NOT_REQUESTED) {
            return;
        }

        if (isBlank(protocolNumber)) {
            throw new WebApplicationException("Protocolo é obrigatório a partir do envio da solicitação.", Response.Status.BAD_REQUEST);
        }

        if (requestDate == null) {
            throw new WebApplicationException("Data da solicitação é obrigatória a partir do envio.", Response.Status.BAD_REQUEST);
        }

        if (status == SolicitationStatus.REIMBURSED) {
            if (reimbursementDate == null) {
                throw new WebApplicationException("Data de reembolso é obrigatória para o status REIMBURSED.", Response.Status.BAD_REQUEST);
            }

            if (amountReceived == null || amountReceived.compareTo(BigDecimal.ZERO) <= 0) {
                throw new WebApplicationException("Valor recebido deve ser maior que zero para o status REIMBURSED.", Response.Status.BAD_REQUEST);
            }
        }
    }

    private void validateStatusTransition(
            SolicitationStatus currentStatus,
            SolicitationStatus newStatus
    ) {
        if (newStatus == null) {
            throw new WebApplicationException("Novo status é obrigatório.", Response.Status.BAD_REQUEST);
        }

        if (currentStatus == newStatus) {
            return;
        }

        boolean valid =
                currentStatus
                        == SolicitationStatus.NOT_REQUESTED
                        && newStatus
                        == SolicitationStatus.UNDER_REVIEW

                || currentStatus
                        == SolicitationStatus.UNDER_REVIEW
                        && (
                        newStatus
                                == SolicitationStatus.AUTHORIZED
                                || newStatus
                                == SolicitationStatus.DENIED
                )

                || currentStatus
                        == SolicitationStatus.AUTHORIZED
                        && newStatus
                        == SolicitationStatus.REIMBURSED;

        if (!valid) {
            throw new WebApplicationException(
                    "Transição de status inválida: "
                            + currentStatus
                            + " -> "
                            + newStatus,
                    Response.Status.BAD_REQUEST
            );
        }
    }

    private void ensureProtocolAvailable(
            String protocolNumber,
            UUID reimbursementId,
            UUID ignoredSolicitationId
    ) {
        if (isBlank(protocolNumber)) {
            return;
        }

        Solicitation existing = Solicitation.find(
                "reimbursement.id = ?1 "
                        + "and protocolNumber = ?2",
                reimbursementId,
                protocolNumber.trim()
        ).firstResult();

        if (existing != null
                && !existing.getId().equals(
                        ignoredSolicitationId
                )) {
            throw new WebApplicationException(
                    "Este protocolo já foi utilizado neste reembolso.",
                    Response.Status.CONFLICT
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
