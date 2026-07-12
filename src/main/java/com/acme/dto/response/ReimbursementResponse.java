package com.acme.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReimbursementResponse(

        UUID id,

        UUID dependentId,

        String dependentName,

        UUID therapyTypeId,

        String therapyTypeName,

        LocalDate referenceMonth,

        Integer sessionsQuantity,

        BigDecimal sessionValue,

        BigDecimal totalAmount,

        String therapistName,

        String therapistPix,

        String description

) {
}
