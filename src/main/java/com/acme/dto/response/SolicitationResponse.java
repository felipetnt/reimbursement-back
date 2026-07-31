package com.acme.dto.response;

import com.acme.domain.enums.SolicitationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SolicitationResponse(
        UUID id,
        UUID reimbursementId,
        Integer attemptNumber,
        SolicitationStatus status,
        String protocolNumber,
        LocalDate requestDate,
        LocalDate reimbursementDate,
        BigDecimal amountReceived,
        String note
) {
}
