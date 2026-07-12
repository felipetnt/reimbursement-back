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
import java.util.stream.Collectors;

@ApplicationScoped
public class SolicitationService {

    @Inject
    SolicitationMapper mapper;

    public List<SolicitationResponse> list() {
        return Solicitation.<Solicitation>listAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public SolicitationResponse findById(UUID id) {
        Solicitation solicitation = Solicitation.findById(id);

        ensureSolicitationExists(solicitation);

        return mapper.toResponse(solicitation);
    }

    public List<SolicitationResponse> listByReimbursement(UUID reimbursementId) {
        Reimbursement reimbursement = Reimbursement.findById(reimbursementId);

        ensureReimbursementExists(reimbursement);

        return Solicitation.<Solicitation>list(
                        "reimbursement.id = ?1 order by attemptNumber asc",
                        reimbursementId
                )
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SolicitationResponse create(CreateSolicitationRequest request) {
        Reimbursement reimbursement = Reimbursement.findById(request.reimbursementId());

        ensureReimbursementExists(reimbursement);

        ensureCanCreateNewAttempt(reimbursement);

        validateStatusRules(
                request.status(),
                request.protocolNumber(),
                request.requestDate(),
                request.reimbursementDate(),
                request.amountReceived()
        );

        ensureProtocolAvailable(request.protocolNumber());

        Integer nextAttemptNumber = getNextAttemptNumber(reimbursement.getId());

        Solicitation solicitation = mapper.toEntity(
                request,
                reimbursement,
                nextAttemptNumber
        );

        solicitation.persist();

        return mapper.toResponse(solicitation);
    }

    @Transactional
    public SolicitationResponse update(UUID id,
                                       UpdateSolicitationRequest request) {
        Solicitation solicitation = Solicitation.findById(id);

        ensureSolicitationExists(solicitation);

        validateStatusTransition(solicitation.getStatus(), request.status());

        validateStatusRules(
                request.status(),
                request.protocolNumber(),
                request.requestDate(),
                request.reimbursementDate(),
                request.amountReceived()
        );

        ensureProtocolAvailableForUpdate(
                request.protocolNumber(),
                solicitation.getId()
        );

        mapper.updateEntity(request, solicitation);

        return mapper.toResponse(solicitation);
    }

    @Transactional
    public void delete(UUID id) {
        Solicitation solicitation = Solicitation.findById(id);

        ensureSolicitationExists(solicitation);

        solicitation.delete();
    }

    private Integer getNextAttemptNumber(UUID reimbursementId) {
        Solicitation lastSolicitation = Solicitation.find(
                "reimbursement.id = ?1 order by attemptNumber desc",
                reimbursementId
        ).firstResult();

        if (lastSolicitation == null) {
            return 1;
        }

        return lastSolicitation.getAttemptNumber() + 1;
    }

    private void ensureCanCreateNewAttempt(Reimbursement reimbursement) {
        Solicitation lastSolicitation = Solicitation.find(
                "reimbursement.id = ?1 order by attemptNumber desc",
                reimbursement.getId()
        ).firstResult();

        if (lastSolicitation == null) {
            return;
        }

        if (lastSolicitation.getStatus() != SolicitationStatus.DENIED) {
            throw new WebApplicationException(
                    "Só é possível criar uma nova tentativa quando a última solicitação estiver negada.",
                    Response.Status.CONFLICT
            );
        }
    }

    private void validateStatusRules(SolicitationStatus status, String protocolNumber, LocalDate requestDate, LocalDate reimbursementDate, BigDecimal amountReceived) {
        if (status == null) {
            throw new WebApplicationException(
                    "Status é obrigatório.",
                    Response.Status.BAD_REQUEST
            );
        }

        if (statusRequiresProtocol(status) && isBlank(protocolNumber)) {
            throw new WebApplicationException(
                    "Protocolo é obrigatório para este status.",
                    Response.Status.BAD_REQUEST
            );
        }

        if (statusRequiresRequestDate(status) && requestDate == null) {
            throw new WebApplicationException(
                    "Data de solicitação é obrigatória para este status.",
                    Response.Status.BAD_REQUEST
            );
        }

        if (status == SolicitationStatus.REIMBURSED) {
            if (reimbursementDate == null) {
                throw new WebApplicationException(
                        "Data de reembolso é obrigatória para status REIMBURSED.",
                        Response.Status.BAD_REQUEST
                );
            }

            if (amountReceived == null || amountReceived.compareTo(BigDecimal.ZERO) <= 0) {
                throw new WebApplicationException(
                        "Valor recebido deve ser maior que zero para status REIMBURSED.",
                        Response.Status.BAD_REQUEST
                );
            }
        }
    }

    private void validateStatusTransition(SolicitationStatus currentStatus,
                                          SolicitationStatus newStatus) {

        if (newStatus == null) {
            throw new WebApplicationException(
                    "Novo status é obrigatório.",
                    Response.Status.BAD_REQUEST
            );
        }

        if (currentStatus == newStatus) {
            return;
        }

        boolean validTransition =
                currentStatus == SolicitationStatus.NOT_REQUESTED
                        && newStatus == SolicitationStatus.UNDER_REVIEW

                        || currentStatus == SolicitationStatus.UNDER_REVIEW
                        && (
                        newStatus == SolicitationStatus.AUTHORIZED
                                || newStatus == SolicitationStatus.DENIED
                )

                        || currentStatus == SolicitationStatus.AUTHORIZED
                        && newStatus == SolicitationStatus.REIMBURSED;

        if (!validTransition) {
            throw new WebApplicationException(
                    "Transição de status inválida: "
                            + currentStatus + " -> " + newStatus,
                    Response.Status.BAD_REQUEST
            );
        }
    }

    private boolean statusRequiresProtocol(SolicitationStatus status) {
        return status == SolicitationStatus.UNDER_REVIEW
                || status == SolicitationStatus.AUTHORIZED
                || status == SolicitationStatus.REIMBURSED
                || status == SolicitationStatus.DENIED;
    }

    private boolean statusRequiresRequestDate(SolicitationStatus status) {
        return status == SolicitationStatus.UNDER_REVIEW
                || status == SolicitationStatus.AUTHORIZED
                || status == SolicitationStatus.REIMBURSED
                || status == SolicitationStatus.DENIED;
    }

    private void ensureProtocolAvailable(String protocolNumber) {
        if (isBlank(protocolNumber)) {
            return;
        }

        long count = Solicitation.count(
                "protocolNumber",
                protocolNumber.trim()
        );

        if (count > 0) {
            throw new WebApplicationException(
                    "Já existe uma solicitação com este protocolo.",
                    Response.Status.CONFLICT
            );
        }
    }

    private void ensureProtocolAvailableForUpdate(String protocolNumber,
                                                  UUID solicitationId) {
        if (isBlank(protocolNumber)) {
            return;
        }

        Solicitation existing = Solicitation.find(
                "protocolNumber",
                protocolNumber.trim()
        ).firstResult();

        if (existing != null && !existing.getId().equals(solicitationId)) {
            throw new WebApplicationException(
                    "Já existe uma solicitação com este protocolo.",
                    Response.Status.CONFLICT
            );
        }
    }

    private void ensureReimbursementExists(Reimbursement reimbursement) {
        if (reimbursement == null) {
            throw new WebApplicationException(
                    "Reembolso não encontrado.",
                    Response.Status.NOT_FOUND
            );
        }
    }

    private void ensureSolicitationExists(Solicitation solicitation) {
        if (solicitation == null) {
            throw new WebApplicationException(
                    "Solicitação não encontrada.",
                    Response.Status.NOT_FOUND
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}