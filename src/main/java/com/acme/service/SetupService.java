package com.acme.service;

import com.acme.domain.enums.UserRole;
import com.acme.domain.model.Family;
import com.acme.domain.model.User;
import com.acme.dto.request.auth.InitializeSystemRequest;
import com.acme.dto.response.AuthResponse;
import com.acme.mapper.AuthMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.Locale;

@ApplicationScoped
public class SetupService {

    @Inject
    PasswordService passwordService;

    @Inject
    JwtService jwtService;

    @Inject
    AuthMapper authMapper;

    @Transactional
    public AuthResponse initialize(
            InitializeSystemRequest request
    ) {
        ensureSystemIsEmpty();

        Family family = new Family();
        family.setName(request.familyName().trim());
        family.persist();

        User admin = new User();
        admin.setName(request.adminName().trim());
        admin.setEmail(normalizeEmail(request.adminEmail()));
        admin.setPasswordHash(
                passwordService.hash(request.adminPassword())
        );
        admin.setRole(UserRole.ADMIN);
        admin.setFamily(family);
        admin.persist();

        String accessToken = jwtService.generate(admin);

        return authMapper.toResponse(
                accessToken,
                jwtService.getExpiresInSeconds(),
                admin
        );
    }

    private void ensureSystemIsEmpty() {
        if (Family.count() > 0 || User.count() > 0) {
            throw new WebApplicationException(
                    "O sistema já foi inicializado.",
                    Response.Status.CONFLICT
            );
        }
    }

    private String normalizeEmail(String email) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
