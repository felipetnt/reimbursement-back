package com.acme.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReimbursementResponse(

        UUID id,

        UUID therapyId,

        UUID dependentId,

        String dependentName,

        UUID professionalId,

        String professionalName,

        String professionalPixKey,

        UUID specialtyId,

        String specialtyName,

        LocalDate referenceMonth,

        Integer sessionsQuantity,

        BigDecimal sessionValue,

        BigDecimal totalAmount,

        String description

) {
}