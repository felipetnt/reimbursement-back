package com.acme.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TherapyResponse(
        UUID id,
        UUID dependentId,
        String dependentName,
        UUID professionalId,
        String professionalName,
        String professionalPixKey,
        UUID specialtyId,
        String specialtyName,
        UUID familyId,
        String familyName,
        BigDecimal defaultSessionValue,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        String description
) {
}
