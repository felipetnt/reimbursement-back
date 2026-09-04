package com.acme.dto.response;

import com.acme.domain.enums.SolicitationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DashboardSolicitationResponse(
        UUID reimbursementId,
        UUID solicitationId,
        Integer attemptNumber,
        UUID dependentId,
        String dependentName,
        UUID familyId,
        String familyName,
        UUID professionalId,
        String professionalName,
        String specialtyName,
        SolicitationStatus status,
        String protocolNumber,
        LocalDate requestDate,
        BigDecimal totalAmount
) {
}