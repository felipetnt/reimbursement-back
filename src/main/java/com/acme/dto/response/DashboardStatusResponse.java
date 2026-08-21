package com.acme.dto.response;

import com.acme.domain.enums.SolicitationStatus;

import java.math.BigDecimal;

public record DashboardStatusResponse(
        SolicitationStatus status,
        long quantity,
        BigDecimal totalAmount
) {
}