package com.acme.dto.response;

import java.util.UUID;

public record ProfessionalResponse(
        UUID id,
        String name,
        String pixKey,
        UUID specialtyId,
        String specialtyName,
        UUID familyId,
        String familyName
) {
}
