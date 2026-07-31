package com.acme.service;

import com.acme.domain.model.User;
import com.acme.dto.auth.LoginRequest;
import com.acme.dto.response.AuthResponse;
import com.acme.mapper.AuthMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.Locale;

@ApplicationScoped
public class AuthService {

    @Inject
    PasswordService passwordService;

    @Inject
    JwtService jwtService;

    @Inject
    AuthMapper authMapper;

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail =
                normalizeEmail(request.email());

        User user = User.find(
                "lower(email) = ?1",
                normalizedEmail
        ).firstResult();

        boolean validCredentials =
                user != null
                        && passwordService.matches(
                        request.password(),
                        user.getPasswordHash()
                );

        if (!validCredentials) {
            throw new WebApplicationException(
                    "E-mail ou senha inválidos.",
                    Response.Status.UNAUTHORIZED
            );
        }

        String accessToken = jwtService.generate(user);

        return authMapper.toResponse(
                accessToken,
                jwtService.getExpiresInSeconds(),
                user
        );
    }

    private String normalizeEmail(String email) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
