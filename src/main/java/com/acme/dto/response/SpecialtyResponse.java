package com.acme.dto.response;

import java.util.UUID;

public record SpecialtyResponse(
        UUID id,
        String name,
        UUID familyId,
        String familyName
) {
}
