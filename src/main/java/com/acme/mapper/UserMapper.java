package com.acme.mapper;

import com.acme.domain.model.Family;
import com.acme.domain.model.User;
import com.acme.dto.request.create.CreateUserRequest;
import com.acme.dto.request.update.UpdateUserRequest;
import com.acme.dto.response.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;
import java.util.UUID;

@ApplicationScoped
public class UserMapper {

    public User toEntity(
            CreateUserRequest request,
            Family family,
            String passwordHash
    ) {
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(normalizeEmail(request.email()));
        user.setPasswordHash(passwordHash);
        user.setRole(request.role());
        user.setFamily(family);
        return user;
    }

    public User updateEntity(UpdateUserRequest request, User user) {
        user.setName(request.name().trim());
        user.setEmail(normalizeEmail(request.email()));
        user.setRole(request.role());
        return user;
    }

    public UserResponse toResponse(User user) {
        Family family = user.getFamily();

        UUID familyId = null;
        String familyName = null;

        if(family != null) {
            familyId = family.getId();
            familyName = family.getName();
        }

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                familyId,
                familyName
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
