package com.acme.dto.response;

import java.util.List;
import java.util.UUID;

public record MyFamilyResponse(
        UUID id,
        String name,
        List<UserResponse> users,
        List<DependentResponse> dependents
) {
}
