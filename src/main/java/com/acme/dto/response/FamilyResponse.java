package com.acme.dto.response;

import java.util.UUID;

public record FamilyResponse(
        UUID id,
        String name
) {
}
