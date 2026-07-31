package com.acme.mapper;

import com.acme.domain.model.Dependent;
import com.acme.domain.model.Reimbursement;
import com.acme.domain.model.Therapy;
import com.acme.dto.response.DependentDetailsResponse;
import com.acme.dto.response.ReimbursementResponse;
import com.acme.dto.response.TherapyResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class DependentDetailsMapper {

    @Inject
    DependentMapper dependentMapper;

    @Inject
    TherapyMapper therapyMapper;

    @Inject
    ReimbursementMapper reimbursementMapper;

    public DependentDetailsResponse toResponse(
            Dependent dependent,
            List<Therapy> therapies,
            List<Reimbursement> reimbursements
    ) {
        List<TherapyResponse> therapyResponses = therapies.stream()
                .map(therapyMapper::toResponse)
                .toList();

        List<ReimbursementResponse> reimbursementResponses = reimbursements.stream()
                .map(reimbursementMapper::toResponse)
                .toList();

        return new DependentDetailsResponse(
                dependentMapper.toResponse(dependent),
                therapyResponses,
                reimbursementResponses
        );
    }
}
