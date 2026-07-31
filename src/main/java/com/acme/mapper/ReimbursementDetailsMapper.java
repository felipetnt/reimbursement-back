package com.acme.mapper;

import com.acme.domain.model.Reimbursement;
import com.acme.domain.model.Solicitation;
import com.acme.dto.response.ReimbursementDetailsResponse;
import com.acme.dto.response.SolicitationResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ReimbursementDetailsMapper {

    @Inject
    ReimbursementMapper reimbursementMapper;

    @Inject
    SolicitationMapper solicitationMapper;

    public ReimbursementDetailsResponse toResponse(
            Reimbursement reimbursement,
            List<Solicitation> solicitations
    ) {
        List<SolicitationResponse> solicitationResponses = solicitations.stream()
                .map(solicitationMapper::toResponse)
                .toList();

        return new ReimbursementDetailsResponse(
                reimbursementMapper.toResponse(reimbursement),
                solicitationResponses
        );
    }
}
