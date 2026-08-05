package com.acme.dto.request.auth;

import com.acme.dto.response.UserResponse;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}
