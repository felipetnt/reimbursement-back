package com.acme.service;

import com.acme.domain.enums.SolicitationStatus;
import com.acme.domain.enums.UserRole;
import com.acme.domain.model.Reimbursement;
import com.acme.domain.model.Solicitation;
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
        if (currentUserService.hasRole(UserRole.ADMIN)) {
            return Solicitation.<Solicitation>list("order by reimbursement.referenceMonth desc, attemptNumber asc")
                    .stream()
                    .map(mapper::toResponse)
                    .toList();
        }

        UUID familyId = familyAccessService.getCurrentFamilyId();

        return Solicitation.<Solicitation>list(
                        "reimbursement.therapy.dependent.family.id = ?1 order by reimbursement.referenceMonth desc, attemptNumber asc",
                        familyId
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public SolicitationResponse findById(UUID id) {
        return mapper.toResponse(findAccessibleSolicitation(id));
    }

    public List<SolicitationResponse> listByReimbursement(UUID reimbursementId) {
        Reimbursement reimbursement = findAccessibleReimbursement(reimbursementId);

        return Solicitation.<Solicitation>list("reimbursement.id = ?1 order by attemptNumber asc", reimbursement.getId())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public SolicitationResponse update(UUID id, UpdateSolicitationRequest request) {
        currentUserService.requireWritePermission();

        Solicitation solicitation = findAccessibleSolicitation(id);

        ensureCurrentAttempt(solicitation);

        SolicitationStatus previousStatus = solicitation.getStatus();

        validateStatusTransition(previousStatus, request.status());

        validateStatusRules(
                request.status(),
                request.protocolNumber(),
                request.requestDate(),
                request.reimbursementDate(),
                request.amountReceived()
        );

        ensureProtocolAvailable(request.protocolNumber(), solicitation.getReimbursement().getId(), solicitation.getId());

        mapper.updateEntity(request, solicitation);

        boolean transitionedToDenied = previousStatus == SolicitationStatus.UNDER_REVIEW && request.status() == SolicitationStatus.DENIED;

        if (transitionedToDenied) {
            createNextAttempt(solicitation.getReimbursement());
        }

        return mapper.toResponse(solicitation);
    }

    private Solicitation findAccessibleSolicitation(UUID id) {
        Solicitation solicitation;

        if (currentUserService.hasRole(UserRole.ADMIN)) {
            solicitation = Solicitation.findById(id);
        } else {
            UUID familyId = familyAccessService.getCurrentFamilyId();
            solicitation = Solicitation.find("id = ?1 and reimbursement.therapy.dependent.family.id = ?2", id, familyId).firstResult();
        }

        if (solicitation == null) {
            throw new WebApplicationException("Solicitação não encontrada.", Response.Status.NOT_FOUND);
        }

        return solicitation;
    }

    private Reimbursement findAccessibleReimbursement(UUID id) {
        Reimbursement reimbursement;

        if (currentUserService.hasRole(UserRole.ADMIN)) {
            reimbursement = Reimbursement.findById(id);
        } else {
            UUID familyId = familyAccessService.getCurrentFamilyId();
            reimbursement = Reimbursement.find("id = ?1 and therapy.dependent.family.id = ?2", id, familyId).firstResult();
        }

        if (reimbursement == null) {
            throw new WebApplicationException("Reembolso não encontrado.", Response.Status.NOT_FOUND);
        }

        return reimbursement;
    }

    private void ensureCurrentAttempt(Solicitation solicitation) {
        Solicitation currentAttempt = Solicitation.find(
                "reimbursement.id = ?1 order by attemptNumber desc",
                solicitation.getReimbursement().getId()
        ).firstResult();

        if (currentAttempt == null || !currentAttempt.getId().equals(solicitation.getId())) {
            throw new WebApplicationException(
                    "Somente a tentativa atual pode ser alterada.",
                    Response.Status.CONFLICT
            );
        }
    }

    private Integer getNextAttemptNumber(UUID reimbursementId) {
        Solicitation last = Solicitation.find("reimbursement.id = ?1 order by attemptNumber desc", reimbursementId).firstResult();

        return last == null ? 1 : last.getAttemptNumber() + 1;
    }

    private void createNextAttempt(Reimbursement reimbursement) {
        Integer attemptNumber = getNextAttemptNumber(reimbursement.getId());

        Solicitation nextAttempt = new Solicitation();
        nextAttempt.setReimbursement(reimbursement);
        nextAttempt.setAttemptNumber(attemptNumber);
        nextAttempt.setStatus(SolicitationStatus.NOT_REQUESTED);

        nextAttempt.persist();
    }

    private void validateStatusRules(SolicitationStatus status, String protocolNumber, LocalDate requestDate, LocalDate reimbursementDate, BigDecimal amountReceived) {
        if (status == null) {
            throw new WebApplicationException("Status é obrigatório.", Response.Status.BAD_REQUEST);
        }

        if (status == SolicitationStatus.NOT_REQUESTED) {
            if (!isBlank(protocolNumber) || requestDate != null || reimbursementDate != null || amountReceived != null) {
                throw new WebApplicationException(
                        "Uma solicitação não requisitada não pode possuir protocolo, data de solicitação, data de reembolso ou valor recebido.",
                        Response.Status.BAD_REQUEST
                );
            }

            return;
        }

        if (isBlank(protocolNumber)) {
            throw new WebApplicationException(
                    "Protocolo é obrigatório a partir do envio da solicitação.",
                    Response.Status.BAD_REQUEST
            );
        }

        if (requestDate == null) {
            throw new WebApplicationException(
                    "Data da solicitação é obrigatória a partir do envio.",
                    Response.Status.BAD_REQUEST
            );
        }

        if (status == SolicitationStatus.REIMBURSED) {
            if (reimbursementDate == null) {
                throw new WebApplicationException(
                        "Data de reembolso é obrigatória para o status REIMBURSED.",
                        Response.Status.BAD_REQUEST
                );
            }

            if (amountReceived == null || amountReceived.compareTo(BigDecimal.ZERO) <= 0) {
                throw new WebApplicationException(
                        "Valor recebido deve ser maior que zero para o status REIMBURSED.",
                        Response.Status.BAD_REQUEST
                );
            }

            return;
        }

        if (reimbursementDate != null || amountReceived != null) {
            throw new WebApplicationException(
                    "Data de reembolso e valor recebido só podem ser informados quando o status for REIMBURSED.",
                    Response.Status.BAD_REQUEST
            );
        }
    }

    private void validateStatusTransition(SolicitationStatus currentStatus, SolicitationStatus newStatus) {
        if (newStatus == null) {
            throw new WebApplicationException("Novo status é obrigatório.", Response.Status.BAD_REQUEST);
        }

        if (currentStatus == newStatus) {
            return;
        }

        boolean valid =
                currentStatus == SolicitationStatus.NOT_REQUESTED && newStatus == SolicitationStatus.UNDER_REVIEW
                        || currentStatus == SolicitationStatus.UNDER_REVIEW && (newStatus == SolicitationStatus.AUTHORIZED || newStatus == SolicitationStatus.DENIED)
                        || currentStatus == SolicitationStatus.AUTHORIZED && newStatus == SolicitationStatus.REIMBURSED;

        if (!valid) {
            throw new WebApplicationException(
                    "Transição de status inválida: " + currentStatus + " -> " + newStatus,
                    Response.Status.BAD_REQUEST
            );
        }
    }

    private void ensureProtocolAvailable(String protocolNumber, UUID reimbursementId, UUID ignoredSolicitationId) {
        if (isBlank(protocolNumber)) {
            return;
        }

        Solicitation existing = Solicitation.find(
                "reimbursement.id = ?1 and protocolNumber = ?2",
                reimbursementId,
                protocolNumber.trim()
        ).firstResult();

        if (existing != null && !existing.getId().equals(ignoredSolicitationId)) {
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