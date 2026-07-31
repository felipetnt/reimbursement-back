package com.acme.mapper;

import com.acme.domain.model.Dependent;
import com.acme.domain.model.Professional;
import com.acme.domain.model.Reimbursement;
import com.acme.domain.model.Therapy;
import com.acme.dto.request.create.CreateReimbursementRequest;
import com.acme.dto.request.update.UpdateReimbursementRequest;
import com.acme.dto.response.ReimbursementResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReimbursementMapper {

    public Reimbursement toEntity(
            CreateReimbursementRequest request,
            Therapy therapy
    ) {
        Reimbursement reimbursement = new Reimbursement();
        reimbursement.setTherapy(therapy);
        reimbursement.setReferenceMonth(request.referenceMonth());
        reimbursement.setSessionsQuantity(request.sessionsQuantity());
        reimbursement.setSessionValue(request.sessionValue());
        reimbursement.setDescription(trimOrNull(request.description()));
        reimbursement.recalculateTotalAmount();
        return reimbursement;
    }

    public Reimbursement updateEntity(
            UpdateReimbursementRequest request,
            Reimbursement reimbursement
    ) {
        reimbursement.setReferenceMonth(request.referenceMonth());
        reimbursement.setSessionsQuantity(request.sessionsQuantity());
        reimbursement.setSessionValue(request.sessionValue());
        reimbursement.setDescription(trimOrNull(request.description()));
        reimbursement.recalculateTotalAmount();
        return reimbursement;
    }

    public ReimbursementResponse toResponse(Reimbursement reimbursement) {
        Therapy therapy = reimbursement.getTherapy();
        Dependent dependent = therapy.getDependent();
        Professional professional = therapy.getProfessional();

        return new ReimbursementResponse(
                reimbursement.getId(),
                therapy.getId(),
                dependent.getId(),
                dependent.getName(),
                professional.getId(),
                professional.getName(),
                professional.getPixKey(),
                professional.getSpecialty().getId(),
                professional.getSpecialty().getName(),
                reimbursement.getReferenceMonth(),
                reimbursement.getSessionsQuantity(),
                reimbursement.getSessionValue(),
                reimbursement.getTotalAmount(),
                reimbursement.getDescription()
        );
    }

    private String trimOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
