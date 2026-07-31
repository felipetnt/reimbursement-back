package com.acme.mapper;

import com.acme.domain.enums.SolicitationStatus;
import com.acme.domain.model.Reimbursement;
import com.acme.domain.model.Solicitation;
import com.acme.dto.request.create.CreateSolicitationRequest;
import com.acme.dto.request.update.UpdateSolicitationRequest;
import com.acme.dto.response.SolicitationResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SolicitationMapper {

    public Solicitation toEntity(
            CreateSolicitationRequest request,
            Reimbursement reimbursement,
            Integer attemptNumber,
            SolicitationStatus initialStatus
    ) {
        Solicitation solicitation = new Solicitation();
        solicitation.setReimbursement(reimbursement);
        solicitation.setAttemptNumber(attemptNumber);
        solicitation.setStatus(initialStatus);
        solicitation.setNote(trimOrNull(request.note()));
        return solicitation;
    }

    public Solicitation updateEntity(
            UpdateSolicitationRequest request,
            Solicitation solicitation
    ) {
        solicitation.setStatus(request.status());
        solicitation.setProtocolNumber(trimOrNull(request.protocolNumber()));
        solicitation.setRequestDate(request.requestDate());
        solicitation.setReimbursementDate(request.reimbursementDate());
        solicitation.setAmountReceived(request.amountReceived());
        solicitation.setNote(trimOrNull(request.note()));
        return solicitation;
    }

    public SolicitationResponse toResponse(Solicitation solicitation) {
        return new SolicitationResponse(
                solicitation.getId(),
                solicitation.getReimbursement().getId(),
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
        return value == null || value.isBlank() ? null : value.trim();
    }
}
