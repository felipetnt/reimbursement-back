package com.acme.dto.response;

import com.acme.domain.enums.UserRole;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        UUID familyId,
        String familyName
) {
}
