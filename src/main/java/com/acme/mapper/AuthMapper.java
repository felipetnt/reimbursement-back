package com.acme.mapper;

import com.acme.domain.model.User;
import com.acme.dto.request.auth.AuthResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthMapper {

    @Inject
    UserMapper userMapper;

    public AuthResponse toResponse(String accessToken, long expiresInSeconds, User user
    ) {
        return new AuthResponse(
                accessToken,
                "Bearer",
                expiresInSeconds,
                userMapper.toResponse(user)
        );
    }
}
