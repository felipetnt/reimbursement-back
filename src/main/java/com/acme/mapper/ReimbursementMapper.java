package com.acme.mapper;

import com.acme.domain.model.Dependent;
import com.acme.domain.model.Reimbursement;
import com.acme.domain.model.TherapyType;
import com.acme.dto.request.create.CreateReimbursementRequest;
import com.acme.dto.request.update.UpdateReimbursementRequest;
import com.acme.dto.response.ReimbursementResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReimbursementMapper {

    public Reimbursement toEntity(CreateReimbursementRequest request, Dependent dependent, TherapyType therapyType) {

        Reimbursement reimbursement = new Reimbursement();

        reimbursement.setDependent(dependent);
        reimbursement.setTherapyType(therapyType);
        reimbursement.setReferenceMonth(request.referenceMonth());
        reimbursement.setSessionsQuantity(request.sessionsQuantity());
        reimbursement.setSessionValue(request.sessionValue());
        reimbursement.setTherapistName(request.therapistName().trim());
        reimbursement.setTherapistPix(request.therapistPix().trim());
        reimbursement.setDescription(trimOrNull(request.description()));

        reimbursement.recalculateTotalAmount();

        return reimbursement;
    }

    public Reimbursement updateEntity(UpdateReimbursementRequest request, Reimbursement reimbursement, Dependent dependent, TherapyType therapyType) {

        reimbursement.setDependent(dependent);
        reimbursement.setTherapyType(therapyType);
        reimbursement.setReferenceMonth(request.referenceMonth());
        reimbursement.setSessionsQuantity(request.sessionsQuantity());
        reimbursement.setSessionValue(request.sessionValue());
        reimbursement.setTherapistName(request.therapistName().trim());
        reimbursement.setTherapistPix(request.therapistPix().trim());
        reimbursement.setDescription(trimOrNull(request.description()));

        reimbursement.recalculateTotalAmount();

        return reimbursement;
    }

    public ReimbursementResponse toResponse(Reimbursement reimbursement) {

        return new ReimbursementResponse(
                reimbursement.getId(),
                reimbursement.getDependent().getId(),
                reimbursement.getDependent().getName(),
                reimbursement.getTherapyType().getId(),
                reimbursement.getTherapyType().getName(),
                reimbursement.getReferenceMonth(),
                reimbursement.getSessionsQuantity(),
                reimbursement.getSessionValue(),
                reimbursement.getTotalAmount(),
                reimbursement.getTherapistName(),
                reimbursement.getTherapistPix(),
                reimbursement.getDescription()
        );
    }

    private String trimOrNull(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}