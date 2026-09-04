package com.acme.dto.response;

import com.acme.domain.enums.SolicitationStatus;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStatusResponse(
        SolicitationStatus status,
        long quantity,
        BigDecimal totalAmount,
        List<DashboardSolicitationResponse> solicitations
) {
}