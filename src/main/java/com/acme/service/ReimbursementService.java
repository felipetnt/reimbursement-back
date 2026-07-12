package com.acme.service;

import com.acme.domain.enums.SolicitationStatus;
import com.acme.domain.model.Dependent;
import com.acme.domain.model.Reimbursement;
import com.acme.domain.model.Solicitation;
import com.acme.domain.model.TherapyType;
import com.acme.dto.request.create.CreateReimbursementRequest;
import com.acme.dto.request.update.UpdateReimbursementRequest;
import com.acme.dto.response.ReimbursementResponse;
import com.acme.mapper.ReimbursementMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReimbursementService {

    @Inject
    ReimbursementMapper mapper;

    public List<ReimbursementResponse> list() {
        return Reimbursement.<Reimbursement>listAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public ReimbursementResponse findById(UUID id) {
        Reimbursement reimbursement = Reimbursement.findById(id);

        ensureReimbursementExists(reimbursement);

        return mapper.toResponse(reimbursement);
    }

    public List<ReimbursementResponse> listByDependent(UUID dependentId) {
        Dependent dependent = Dependent.findById(dependentId);

        ensureDependentExists(dependent);

        return Reimbursement.<Reimbursement>list("dependent.id", dependentId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReimbursementResponse create(CreateReimbursementRequest request) {
        Dependent dependent = Dependent.findById(request.dependentId());

        ensureDependentExists(dependent);

        TherapyType therapyType = TherapyType.findById(request.therapyTypeId());

        ensureTherapyTypeExists(therapyType);

        ensureTherapyTypeBelongsToDependent(dependent, therapyType);

        Reimbursement reimbursement = mapper.toEntity(request, dependent, therapyType);

        Solicitation initialSolicitation = new Solicitation();
        initialSolicitation.setAttemptNumber(1);
        initialSolicitation.setStatus(SolicitationStatus.NOT_REQUESTED);

        reimbursement.addSolicitation(initialSolicitation);

        reimbursement.persist();

        return mapper.toResponse(reimbursement);
    }

    @Transactional
    public ReimbursementResponse update(UUID id, UpdateReimbursementRequest request) {
        Reimbursement reimbursement = Reimbursement.findById(id);

        ensureReimbursementExists(reimbursement);

        Dependent dependent = Dependent.findById(request.dependentId());

        ensureDependentExists(dependent);

        TherapyType therapyType = TherapyType.findById(request.therapyTypeId());

        ensureTherapyTypeExists(therapyType);

        ensureTherapyTypeBelongsToDependent(dependent, therapyType);

        mapper.updateEntity(request, reimbursement, dependent, therapyType);

        return mapper.toResponse(reimbursement);
    }

    @Transactional
    public void delete(UUID id) {
        Reimbursement reimbursement = Reimbursement.findById(id);

        ensureReimbursementExists(reimbursement);

        reimbursement.delete();
    }

    private void ensureReimbursementExists(Reimbursement reimbursement) {
        if (reimbursement == null) {
            throw new WebApplicationException(
                    "Reembolso não encontrado.",
                    Response.Status.NOT_FOUND
            );
        }
    }

    private void ensureDependentExists(Dependent dependent) {
        if (dependent == null) {
            throw new WebApplicationException(
                    "Dependente não encontrado.",
                    Response.Status.NOT_FOUND
            );
        }
    }

    private void ensureTherapyTypeExists(TherapyType therapyType) {
        if (therapyType == null) {
            throw new WebApplicationException(
                    "Tipo de terapia não encontrado.",
                    Response.Status.NOT_FOUND
            );
        }
    }

    private static void ensureTherapyTypeBelongsToDependent(Dependent dependent,
                                                     TherapyType therapyType) {
        UUID dependentId = dependent.getId();
        UUID therapyTypeDependentId = therapyType.getDependent().getId();

        if (!dependentId.equals(therapyTypeDependentId)) {
            throw new WebApplicationException(
                    "O tipo de terapia informado não pertence ao dependente informado.",
                    Response.Status.BAD_REQUEST
            );
        }
    }
}