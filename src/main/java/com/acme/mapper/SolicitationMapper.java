package com.acme.mapper;

import com.acme.domain.model.Reimbursement;
import com.acme.domain.model.Solicitation;
import com.acme.dto.request.create.CreateSolicitationRequest;
import com.acme.dto.request.update.UpdateSolicitationRequest;
import com.acme.dto.response.SolicitationResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SolicitationMapper {

    public Solicitation toEntity(CreateSolicitationRequest request,
                                 Reimbursement reimbursement,
                                 Integer attemptNumber) {

        Solicitation solicitation = new Solicitation();

        solicitation.setReimbursement(reimbursement);
        solicitation.setAttemptNumber(attemptNumber);
        solicitation.setStatus(request.status());
        solicitation.setProtocolNumber(trimOrNull(request.protocolNumber()));
        solicitation.setRequestDate(request.requestDate());
        solicitation.setReimbursementDate(request.reimbursementDate());
        solicitation.setAmountReceived(request.amountReceived());
        solicitation.setNote(trimOrNull(request.note()));

        return solicitation;
    }

    public Solicitation updateEntity(UpdateSolicitationRequest request,
                                     Solicitation solicitation) {

        solicitation.setStatus(request.status());
        solicitation.setProtocolNumber(trimOrNull(request.protocolNumber()));
        solicitation.setRequestDate(request.requestDate());
        solicitation.setReimbursementDate(request.reimbursementDate());
        solicitation.setAmountReceived(request.amountReceived());
        solicitation.setNote(trimOrNull(request.note()));

        return solicitation;
    }

    public SolicitationResponse toResponse(Solicitation solicitation) {

        Reimbursement reimbursement = solicitation.getReimbursement();

        return new SolicitationResponse(
                solicitation.getId(),
                reimbursement.getId(),
                reimbursement.getDependent().getId(),
                reimbursement.getDependent().getName(),
                reimbursement.getTherapyType().getId(),
                reimbursement.getTherapyType().getName(),
                solicitation.getAttemptNumber(),
                solicitation.getStatus(),
                solicitation.getProtocolNumber(),
                solicitation.getRequestDate(),
                solicitation.getReimbursementDate(),
                solicitation.getAmountReceived(),
                solicitation.getNote()
        );
    }

    private String trimOrNull(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}