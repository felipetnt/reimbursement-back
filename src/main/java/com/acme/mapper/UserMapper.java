package com.acme.mapper;

import com.acme.domain.model.Family;
import com.acme.domain.model.User;
import com.acme.dto.request.create.CreateUserRequest;
import com.acme.dto.request.update.UpdateUserRequest;
import com.acme.dto.response.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserMapper {

    public User toEntity(CreateUserRequest request,
                         Family family) {

        User user = new User();

        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setRole(request.role());
        user.setFamily(family);

        return user;
    }

    public User updateEntity(UpdateUserRequest request,
                             User user,
                             Family family) {

        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
        user.setFamily(family);

        return user;
    }

    public UserResponse toResponse(User entity) {

        return new UserResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getRole(),
                entity.getFamily().getId(),
                entity.getFamily().getName()
        );
    }
}